import { useEffect, useState } from "react";
import {
  BrowserRouter as BrowserRouter,
  HashRouter as HashRouter,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";
import MainLayout from "./layout/MainLayout";
import Dashboard from "./pages/Dashboard";
import Chat from "./pages/Chat";
import AdminDashboard from "./pages/AdminDashboard";
import { LoginPage } from "./pages/Login";
import { SignupPage } from "./pages/Signup";
import { fetchCurrentUser } from "./api";

export type UserRank = {
  id: number;
  name: string;
  level: number;
  minProjects: number;
  maxProjects: number;
  beltColor: string;
  meaning: string;
};

export type CurrentUser = {
  id: number;
  email: string;
  displayName?: string;
  completedProjects: number;
  rank: UserRank;
  projectsToNextRank: number;
};

export default function App() {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem("token"));
  const [email, setEmail] = useState<string | null>(() => localStorage.getItem("ronin_email"));
  const [currentUser, setCurrentUser] = useState<CurrentUser | null>(null);

  useEffect(() => {
    if (token) {
      localStorage.setItem("token", token);
    } else {
      localStorage.removeItem("token");
      setCurrentUser(null);
    }
  }, [token]);

  useEffect(() => {
    if (email) {
      localStorage.setItem("ronin_email", email);
    } else {
      localStorage.removeItem("ronin_email");
    }
  }, [email]);

  useEffect(() => {
    if (!token) {
      return;
    }

    const loadUser = async () => {
      try {
        const profile = await fetchCurrentUser(token);
        setCurrentUser({
          ...profile,
          projectsToNextRank: profile.projectsToNextRank ?? 0,
        });
      } catch (error) {
        console.error("Unable to load current user profile:", error);
      }
    };

    void loadUser();
  }, [token]);

  const handleLogin = (nextToken: string, nextEmail: string) => {
    setToken(nextToken);
    setEmail(nextEmail);
  };

  // If OAuth redirected back with token in query params, consume it and update app state
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const t = params.get("token");
    const e = params.get("email");
    if (t) {
      handleLogin(t, e ?? "");
      // Clean URL
      const newUrl = window.location.pathname;
      window.history.replaceState({}, "", newUrl);
    }
  }, []);

  const handleLogout = () => {
    setToken(null);
    setEmail(null);
    setCurrentUser(null);
  };

  const AppRouter = window.location.protocol === "file:" ? HashRouter : BrowserRouter;

  return (
    <AppRouter>
      <Routes>
        {/* Public Routes */}
        <Route path="/login" element={<LoginPage onLogin={handleLogin} />} />
        <Route path="/signup" element={<SignupPage onLogin={handleLogin} />} />

        {/* Protected Routes */}
        <Route
          path="/*"
          element={
            token ? (
              <MainLayout
                userEmail={email}
                user={currentUser}
                onLogout={handleLogout}
              >
                <Routes>
                  <Route
                    path="/"
                    element={
                      <Dashboard
                        token={token}
                        currentUser={currentUser}
                        onLogin={handleLogin}
                        onLogout={handleLogout}
                      />
                    }
                  />
                  <Route
                    path="/admin"
                    element={<AdminDashboard token={token} />}
                  />
                </Routes>
              </MainLayout>
            ) : (
              <Navigate to="/login" replace />
            )
          }
        />
      </Routes>
    </AppRouter>
  );
}
