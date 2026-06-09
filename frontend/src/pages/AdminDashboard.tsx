import { useEffect, useState } from "react";
import {
  fetchAdminDashboardStats,
  fetchAdminUsersStats,
  fetchAdminProjectsStats,
  fetchAdminLoginEvents,
} from "../api";

interface AdminStats {
  totalUsers: number;
  totalProjects: number;
  totalLogins: number;
  failedLogins?: number;
  uniqueUsersLoggedInToday?: number;
}

interface UserStats {
  userId: number;
  email: string;
  displayName?: string;
  completedProjects: number;
}

interface ProjectStats {
  projectId: number;
  name: string;
  ownerEmail: string;
  status: string;
  phase: string;
}

interface LoginEvent {
  id: number;
  userEmail: string;
  loginTime: string;
  success: boolean;
}

export default function AdminDashboard({ token }: { token: string }) {
  const [stats, setStats] = useState<AdminStats | null>(null);
  const [users, setUsers] = useState<UserStats[]>([]);
  const [projects, setProjects] = useState<ProjectStats[]>([]);
  const [loginEvents, setLoginEvents] = useState<LoginEvent[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchAdminData = async () => {
      try {
        setLoading(true);
        setError(null);

        const [statsRes, usersRes, projectsRes, loginsRes] = await Promise.all([
          fetchAdminDashboardStats(token),
          fetchAdminUsersStats(token),
          fetchAdminProjectsStats(token),
          fetchAdminLoginEvents(token),
        ]);

        setStats(statsRes);
        setUsers(usersRes);
        setProjects(projectsRes);
        setLoginEvents(loginsRes);
      } catch (err) {
        console.error("Failed to load admin data:", err);
        setError(
          err instanceof Error ? err.message : "Failed to load admin data"
        );
      } finally {
        setLoading(false);
      }
    };

    void fetchAdminData();
  }, [token]);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="mb-4 text-lg font-semibold text-slate-900">
            Loading admin dashboard...
          </div>
          <div className="animate-spin h-8 w-8 border-4 border-slate-300 border-t-slate-900 rounded-full mx-auto"></div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="mb-4 text-lg font-semibold text-red-600">Error</div>
          <p className="text-slate-700">{error}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-slate-900">Admin Dashboard</h1>
      </div>

      {/* Summary Stats */}
      {stats && (
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div
            className="rounded-lg p-6 text-white"
            style={{ background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)" }}
          >
            <div className="text-sm font-semibold opacity-90">Total Users</div>
            <div className="mt-2 text-3xl font-bold">{stats.totalUsers}</div>
          </div>
          <div
            className="rounded-lg p-6 text-white"
            style={{ background: "linear-gradient(135deg, #f093fb 0%, #f5576c 100%)" }}
          >
            <div className="text-sm font-semibold opacity-90">Total Projects</div>
            <div className="mt-2 text-3xl font-bold">{stats.totalProjects}</div>
          </div>
          <div
            className="rounded-lg p-6 text-white"
            style={{ background: "linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)" }}
          >
            <div className="text-sm font-semibold opacity-90">Total Logins</div>
            <div className="mt-2 text-3xl font-bold">{stats.totalLogins}</div>
          </div>
          <div
            className="rounded-lg p-6 text-white"
            style={{ background: "linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)" }}
          >
            <div className="text-sm font-semibold opacity-90">Failed Logins</div>
            <div className="mt-2 text-3xl font-bold">{stats.failedLogins ?? 0}</div>
          </div>
        </div>
      )}

      {/* Users Table */}
      <div className="rounded-lg border border-slate-300 bg-white p-6">
        <h2 className="mb-4 text-xl font-semibold text-slate-900">Top Users</h2>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-slate-200">
                <th className="px-4 py-3 text-left text-sm font-semibold text-slate-700">
                  Email
                </th>
                <th className="px-4 py-3 text-left text-sm font-semibold text-slate-700">
                  Display Name
                </th>
                <th className="px-4 py-3 text-left text-sm font-semibold text-slate-700">
                  Completed Projects
                </th>
              </tr>
            </thead>
            <tbody>
              {users.length > 0 ? (
                users.slice(0, 10).map((user) => (
                  <tr
                    key={user.userId}
                    className="border-b border-slate-200 hover:bg-slate-50"
                  >
                    <td className="px-4 py-3 text-sm text-slate-900">
                      {user.email}
                    </td>
                    <td className="px-4 py-3 text-sm text-slate-600">
                      {user.displayName ?? "-"}
                    </td>
                    <td className="px-4 py-3 text-sm text-slate-900 font-semibold">
                      {user.completedProjects}
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={3} className="px-4 py-3 text-center text-slate-500">
                    No users found
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Projects Table */}
      <div className="rounded-lg border border-slate-300 bg-white p-6">
        <h2 className="mb-4 text-xl font-semibold text-slate-900">Recent Projects</h2>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-slate-200">
                <th className="px-4 py-3 text-left text-sm font-semibold text-slate-700">
                  Name
                </th>
                <th className="px-4 py-3 text-left text-sm font-semibold text-slate-700">
                  Owner Email
                </th>
                <th className="px-4 py-3 text-left text-sm font-semibold text-slate-700">
                  Phase
                </th>
                <th className="px-4 py-3 text-left text-sm font-semibold text-slate-700">
                  Status
                </th>
              </tr>
            </thead>
            <tbody>
              {projects.length > 0 ? (
                projects.slice(0, 10).map((project) => (
                  <tr
                    key={project.projectId}
                    className="border-b border-slate-200 hover:bg-slate-50"
                  >
                    <td className="px-4 py-3 text-sm text-slate-900 font-medium">
                      {project.name}
                    </td>
                    <td className="px-4 py-3 text-sm text-slate-600">
                      {project.ownerEmail}
                    </td>
                    <td className="px-4 py-3 text-sm text-slate-600">
                      {project.phase}
                    </td>
                    <td className="px-4 py-3 text-sm">
                      <span
                        className={`inline-flex items-center rounded-full px-3 py-1 text-xs font-medium ${
                          project.status === "completed"
                            ? "bg-green-100 text-green-800"
                            : project.status === "in_progress"
                            ? "bg-blue-100 text-blue-800"
                            : "bg-slate-100 text-slate-800"
                        }`}
                      >
                        {project.status}
                      </span>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={4} className="px-4 py-3 text-center text-slate-500">
                    No projects found
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Login Events Table */}
      <div className="rounded-lg border border-slate-300 bg-white p-6">
        <h2 className="mb-4 text-xl font-semibold text-slate-900">Recent Login Events</h2>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-slate-200">
                <th className="px-4 py-3 text-left text-sm font-semibold text-slate-700">
                  User Email
                </th>
                <th className="px-4 py-3 text-left text-sm font-semibold text-slate-700">
                  Login Time
                </th>
                <th className="px-4 py-3 text-left text-sm font-semibold text-slate-700">
                  Status
                </th>
              </tr>
            </thead>
            <tbody>
              {loginEvents.length > 0 ? (
                loginEvents.slice(0, 10).map((event) => (
                  <tr
                    key={event.id}
                    className="border-b border-slate-200 hover:bg-slate-50"
                  >
                    <td className="px-4 py-3 text-sm text-slate-900">
                      {event.userEmail}
                    </td>
                    <td className="px-4 py-3 text-sm text-slate-600">
                      {new Date(event.loginTime).toLocaleString()}
                    </td>
                    <td className="px-4 py-3 text-sm">
                      <span
                        className={`inline-flex items-center rounded-full px-3 py-1 text-xs font-medium ${
                          event.success
                            ? "bg-green-100 text-green-800"
                            : "bg-red-100 text-red-800"
                        }`}
                      >
                        {event.success ? "Success" : "Failed"}
                      </span>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={3} className="px-4 py-3 text-center text-slate-500">
                    No login events found
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
