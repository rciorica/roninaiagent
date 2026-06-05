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
      <div className="rounded-3xl border border-emerald-200 bg-gradient-to-br from-emerald-100 via-emerald-50 to-white p-4 text-slate-900">
        <div className="flex items-center justify-between gap-4">
          <div>
            <h3 className="text-xl font-semibold">{project.name}</h3>
            <p className="text-sm text-slate-600">Final product ready for review.</p>
          </div>
          <span className="rounded-full bg-emerald-900 px-3 py-1 text-sm text-white">{project.status}</span>
        </div>
        <p className="mt-2 text-gray-600">{project.description}</p>
        <div className="mt-3 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex items-center gap-2 text-sm text-gray-600">
            <span>Phase:</span>
            <strong>{project.phase}</strong>
          </div>
          <button
            type="button"
            onClick={() => onSelectProject(project.id)}
            className="rounded-xl bg-slate-900 px-4 py-2 text-sm text-white hover:bg-slate-700"
          >
            Review final product
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="rounded-3xl border border-gray-200 bg-gradient-to-br from-slate-50 via-slate-100 to-white p-4 text-slate-900">
      <div className="flex items-start justify-between gap-4">
        <div className="flex-1">
          <div className="flex items-center gap-3">
            <h3 className="text-xl font-semibold">{project.name}</h3>
            {currentUser && (
              <BeltBadge color={currentUser.rank.beltColor} />
            )}
          </div>
          <p className="mt-2 text-gray-600">{project.description}</p>
        </div>
        <span className="rounded-full bg-slate-900 px-3 py-1 text-sm text-white whitespace-nowrap">{project.status}</span>
      </div>

      <div className="mt-3 flex flex-col gap-3">
        <div className="flex items-center justify-between text-sm text-gray-600">
          <div className="flex items-center gap-2">
            <span>Phase:</span>
            <strong>{project.phase}</strong>
          </div>
          {currentUser && currentUser.projectsToNextRank > 0 && (
            <div className="rounded-full bg-slate-200 px-3 py-1 text-xs text-slate-700 font-medium">
              {currentUser.projectsToNextRank} project{currentUser.projectsToNextRank !== 1 ? "s" : ""} to next rank
            </div>
          )}
        </div>

        {currentUser && currentUser.projectsToNextRank === 0 && (
          <div className="rounded-xl bg-amber-100 px-3 py-2 text-xs text-amber-900">
            🎉 Ready for rank up! Complete this project to advance to the next belt.
          </div>
        )}
      </div>

      <button
        type="button"
        onClick={() => onSelectProject(project.id)}
        className="mt-4 w-full rounded-xl bg-slate-900 px-4 py-2 text-sm text-white hover:bg-slate-700"
      >
        Continue project
      </button>
    </div>
  );
}
