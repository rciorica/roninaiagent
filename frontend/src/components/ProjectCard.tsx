import { type CurrentUser } from "../App";
import BeltBadge from "./BeltBadge";

type Project = {
  id: number;
  name: string;
  description: string;
  phase: string;
  status: string;
  repoUrl?: string;
};

type ProjectCardProps = {
  project: Project;
  isCompleted: boolean;
  currentUser: CurrentUser | null;
  onSelectProject: (projectId: number) => void;
};

export default function ProjectCard({ project, isCompleted, currentUser, onSelectProject }: ProjectCardProps) {
  if (isCompleted) {
    return (
      <div className="rounded-2xl sm:rounded-3xl border border-emerald-200 bg-gradient-to-br from-emerald-100 via-emerald-50 to-white p-3 sm:p-4 text-slate-900">
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-2 sm:gap-4">
          <div className="flex-1 min-w-0">
            <h3 className="text-lg sm:text-xl font-semibold break-words">{project.name}</h3>
            <p className="text-xs sm:text-sm text-slate-600">Final product ready for review.</p>
          </div>
          <span className="rounded-full bg-emerald-900 px-2 sm:px-3 py-1 text-xs sm:text-sm text-white whitespace-nowrap flex-shrink-0">{project.status}</span>
        </div>
        <p className="mt-2 text-xs sm:text-sm text-gray-600 break-words">{project.description}</p>
        <div className="mt-3 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex items-center gap-2 text-xs sm:text-sm text-gray-600">
            <span>Phase:</span>
            <strong>{project.phase}</strong>
          </div>
          <button
            type="button"
            onClick={() => onSelectProject(project.id)}
            className="rounded-lg sm:rounded-xl bg-slate-900 px-3 sm:px-4 py-2 text-xs sm:text-sm text-white hover:bg-slate-700 whitespace-nowrap"
          >
            Review final product
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="rounded-2xl sm:rounded-3xl border border-gray-200 bg-gradient-to-br from-slate-50 via-slate-100 to-white p-3 sm:p-4 text-slate-900">
      <div className="flex flex-col sm:flex-row items-start justify-between gap-2 sm:gap-4">
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 flex-wrap">
            <h3 className="text-lg sm:text-xl font-semibold break-words">{project.name}</h3>
            {currentUser && (
              <BeltBadge color={currentUser.rank.beltColor} />
            )}
          </div>
          <p className="mt-2 text-xs sm:text-sm text-gray-600 break-words">{project.description}</p>
        </div>
        <span className="rounded-full bg-slate-900 px-2 sm:px-3 py-1 text-xs sm:text-sm text-white whitespace-nowrap flex-shrink-0">{project.status}</span>
      </div>

      <div className="mt-3 flex flex-col gap-2 sm:gap-3">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 text-xs sm:text-sm text-gray-600">
          <div className="flex items-center gap-2">
            <span>Phase:</span>
            <strong>{project.phase}</strong>
          </div>
          {currentUser && currentUser.projectsToNextRank > 0 && (
            <div className="rounded-full bg-slate-200 px-2 sm:px-3 py-1 text-xs text-slate-700 font-medium whitespace-nowrap">
              {currentUser.projectsToNextRank} project{currentUser.projectsToNextRank !== 1 ? "s" : ""} to next rank
            </div>
          )}
        </div>

        {currentUser && currentUser.projectsToNextRank === 0 && (
          <div className="rounded-lg sm:rounded-xl bg-amber-100 px-2 sm:px-3 py-2 text-xs text-amber-900">
            🎉 Ready for rank up! Complete this project to advance to the next belt.
          </div>
        )}
      </div>

      <button
        type="button"
        onClick={() => onSelectProject(project.id)}
        className="mt-4 w-full rounded-lg sm:rounded-xl bg-slate-900 px-3 sm:px-4 py-2 text-xs sm:text-sm text-white hover:bg-slate-700"
      >
        Continue project
      </button>
    </div>
  );
}
