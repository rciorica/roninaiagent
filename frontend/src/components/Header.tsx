import kyokushinLogo from "../assets/Kyokushin Karate Logo Vector.svg";
import BeltBadge from "./BeltBadge";
import type { CurrentUser } from "../App";

export default function Header({
  userEmail,
  user,
  onLogout,
}: {
  userEmail: string | null;
  user: CurrentUser | null;
  onLogout: () => void;
}) {
  return (
    <header
      className="min-h-16 border-b flex items-center justify-between px-6 py-4 text-slate-950 shadow-lg"
      style={{ background: "linear-gradient(135deg, #e6e8eb 0%, #788ca0 35%, #959597 65%, #f0f2f5 100%)" }}
    >
      <div className="flex items-center gap-4">
        <img
          src={kyokushinLogo}
          alt="Kyokushin logo"
          className="h-12 w-12 rounded-full object-contain bg-slate-200"
        />
        <div>
          <h2 className="text-xl font-semibold text-slate-950">Ronin Dashboard</h2>
          <p className="text-sm text-slate-700">Build, test, and launch AI-backed projects</p>
          <p className="text-sm text-slate-700">The ultimate truth society</p>
        </div>
      </div>

      <div className="flex items-center gap-4">
        {user ? (
          <div className="flex items-center gap-3 rounded-2xl border border-slate-200 px-3 py-2 text-slate-900 shadow-md ring-1 ring-slate-200" style={{ background: "linear-gradient(135deg, #e6e8eb 0%, #788ca0 35%, #959597 65%, #f0f2f5 100%)" }}>
            <BeltBadge color={user.rank.beltColor} label={user.rank.name} />
            <div className="text-sm text-slate-600">
              <div className="font-medium text-slate-900">{user.displayName ?? user.email}</div>
              <div className="text-xs text-slate-500">{user.completedProjects} completed projects</div>
            </div>
          </div>
        ) : userEmail ? (
          <span className="text-sm text-gray-600">{userEmail}</span>
        ) : (
          <span className="text-sm text-gray-600">Not signed in</span>
        )}
        <button
          onClick={onLogout}
          className="rounded-md bg-slate-950 px-3 py-1 text-white hover:bg-slate-800"
        >
          Logout
        </button>
      </div>
    </header>
  );
}
