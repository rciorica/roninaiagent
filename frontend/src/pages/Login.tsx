import { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { verifyEmail, verifyPassword } from "../api";
import BeltBadge from "../components/BeltBadge";

interface UserData {
  userExists: boolean;
  email: string;
  username?: string;
  rank?: {
    name: string;
    beltColor: string;
    kyuLevel: number;
  };
  sessionId?: string;
}

interface LoginPageProps {
  onLogin: (token: string, email: string) => void;
}

export function LoginPage({ onLogin }: LoginPageProps) {
  const navigate = useNavigate();
  const location = useLocation();

  const [step, setStep] = useState(1); // 1: email, 2: password
  const [email, setEmail] = useState(location.state?.email || "");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [userData, setUserData] = useState<UserData | null>(null);

  /**
   * Step 1: Verify email and load user data in background
   */
  const handleEmailSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const data: UserData = await verifyEmail({ email });
      setUserData(data);

      if (data.userExists) {
        // User exists - move to password step
        setStep(2);
      } else {
        // User doesn't exist - offer signup
        setError("No account found. Would you like to sign up?");
      }
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Verification failed. Please try again."
      );
    } finally {
      setLoading(false);
    }
  };

  /**
   * Step 2: Verify password and login
   */
  const handlePasswordSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    if (!userData?.sessionId) {
      setError("Session expired. Please try again.");
      setLoading(false);
      return;
    }

    try {
      const loginData = await verifyPassword({
        sessionId: userData.sessionId,
        password,
      });
      
      // Store token
      localStorage.setItem("token", loginData.token);
      onLogin(loginData.token, email);
      
      // Redirect to dashboard
      navigate("/");
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Login failed. Please try again."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-8 px-3 sm:px-4">
      <div className="max-w-md w-full">
        {/* Header */}
        <div className="text-center mb-6 sm:mb-8">
          <div className="flex justify-center mb-4">
            <BeltBadge color="white" size="lg" />
          </div>
          <h1 className="text-2xl sm:text-3xl font-bold text-slate-900">Ronin</h1>
          <p className="text-xs sm:text-sm text-gray-600 mt-2">
            {step === 1 ? "Enter your email" : "Enter your password"}
          </p>
        </div>

        {/* Card */}
        <div className="bg-white rounded-2xl sm:rounded-3xl border border-gray-200 p-4 sm:p-8 shadow-sm">
          {error && (
            <div className="mb-4 sm:mb-6 p-3 sm:p-4 rounded-lg sm:rounded-xl bg-red-50 border border-red-200 text-red-900 text-xs sm:text-sm">
              {error}
              {!userData?.userExists && (
                <button
                  onClick={() => navigate("/signup")}
                  className="block mt-2 sm:mt-3 font-medium text-red-700 hover:text-red-900 underline"
                >
                  Create an account
                </button>
              )}
            </div>
          )}

          {/* Step 1: Email */}
          {step === 1 && (
            <form onSubmit={handleEmailSubmit} className="space-y-3 sm:space-y-4">
              <div>
                <label
                  htmlFor="email"
                  className="block text-xs sm:text-sm font-medium text-slate-900 mb-1"
                >
                  Email or username
                </label>
                <input
                  id="email"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="your@email.com"
                  className="w-full rounded-lg sm:rounded-xl border border-gray-300 bg-gray-50 px-3 sm:px-4 py-2 sm:py-3 text-xs sm:text-sm text-gray-900 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-slate-900"
                  disabled={loading}
                  autoFocus
                />
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full rounded-lg sm:rounded-xl bg-slate-900 px-3 sm:px-4 py-2 sm:py-3 text-xs sm:text-sm text-white font-medium hover:bg-slate-700 disabled:bg-gray-400 transition"
              >
                {loading ? "Verifying..." : "Next"}
              </button>
            </form>
          )}

          {/* Step 2: Password */}
          {step === 2 && userData && (
            <form onSubmit={handlePasswordSubmit} className="space-y-4 sm:space-y-6">
              {/* User Info Display */}
              <div className="flex items-center gap-2 sm:gap-4 p-2 sm:p-4 rounded-lg sm:rounded-2xl bg-slate-50 border border-slate-200">
                {userData.rank && (
                  <BeltBadge color={userData.rank.beltColor} size="md" />
                )}
                <div className="min-w-0">
                  <p className="font-medium text-xs sm:text-sm text-slate-900 truncate">
                    {userData.username || userData.email}
                  </p>
                  {userData.rank && (
                    <p className="text-xs text-slate-600">{userData.rank.name}</p>
                  )}
                </div>
              </div>

              <div>
                <label
                  htmlFor="password"
                  className="block text-xs sm:text-sm font-medium text-slate-900 mb-1"
                >
                  Password
                </label>
                <input
                  id="password"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full rounded-lg sm:rounded-xl border border-gray-300 bg-gray-50 px-3 sm:px-4 py-2 sm:py-3 text-xs sm:text-sm text-gray-900 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-slate-900"
                  disabled={loading}
                  autoFocus
                />
              </div>

              <div className="flex gap-2 sm:gap-3">
                <button
                  type="button"
                  onClick={() => setStep(1)}
                  disabled={loading}
                  className="flex-1 rounded-lg sm:rounded-xl border border-gray-300 px-3 sm:px-4 py-2 sm:py-3 text-xs sm:text-sm text-slate-900 font-medium hover:bg-gray-50 disabled:bg-gray-100 transition"
                >
                  Back
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="flex-1 rounded-lg sm:rounded-xl bg-slate-900 px-3 sm:px-4 py-2 sm:py-3 text-xs sm:text-sm text-white font-medium hover:bg-slate-700 disabled:bg-gray-400 transition"
                >
                  {loading ? "Logging in..." : "Sign in"}
                </button>
              </div>
            </form>
          )}

          {/* Sign Up Link */}
          <p className="text-center text-xs sm:text-sm text-gray-600 mt-4 sm:mt-6">
            Don't have an account?{" "}
            <a
              href={window.location.protocol === 'file:' ? '#/signup' : '/signup'}
              className="font-medium text-slate-900 hover:underline"
            >
              Sign up
            </a>
          </p>
        </div>
      </div>
    </div>
  );
}
