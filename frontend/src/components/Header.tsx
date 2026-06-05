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
      className="min-h-16 border-b flex flex-col sm:flex-row items-start sm:items-center justify-between px-2 sm:px-4 lg:px-6 py-3 sm:py-4 text-slate-950 shadow-lg gap-3 sm:gap-4"
      style={{ background: "linear-gradient(135deg, #e6e8eb 0%, #788ca0 35%, #959597 65%, #f0f2f5 100%)" }}
    >
      <div className="flex items-center gap-2 sm:gap-4">
        <img
          src={kyokushinLogo}
          alt="Kyokushin logo"
          className="h-10 sm:h-12 w-10 sm:w-12 rounded-full object-contain bg-slate-200"
        />
        <div>
          <h2 className="text-lg sm:text-xl font-semibold text-slate-950">Ronin Dashboard</h2>
          <p className="text-xs sm:text-sm text-slate-700">Build, test, and launch AI-backed projects</p>
          <p className="hidden sm:block text-xs sm:text-sm text-slate-700">The ultimate truth society</p>
        </div>
      </div>

      <div className="flex items-center gap-2 sm:gap-4 w-full sm:w-auto">
        {user ? (
          <div className="hidden sm:flex items-center gap-2 sm:gap-3 rounded-2xl border border-slate-200 px-2 sm:px-3 py-1 sm:py-2 text-slate-900 shadow-md ring-1 ring-slate-200" style={{ background: "linear-gradient(135deg, #e6e8eb 0%, #788ca0 35%, #959597 65%, #f0f2f5 100%)" }}>
            <BeltBadge color={user.rank.beltColor} label={user.rank.name} />
            <div className="text-xs sm:text-sm text-slate-600">
              <div className="font-medium text-slate-900 text-xs sm:text-sm">{user.displayName ?? user.email}</div>
              <div className="text-xs text-slate-500 hidden sm:block">{user.completedProjects} completed projects</div>
            </div>
          </div>
        ) : userEmail ? (
          <span className="text-xs sm:text-sm text-gray-600">{userEmail}</span>
        ) : (
          <span className="text-xs sm:text-sm text-gray-600">Not signed in</span>
        )}
        <button
          onClick={onLogout}
          className="rounded-md bg-slate-950 px-2 sm:px-3 py-1 text-xs sm:text-sm text-white hover:bg-slate-800 whitespace-nowrap"
        >
          Logout
        </button>
      </div>
    </header>
  );
}
