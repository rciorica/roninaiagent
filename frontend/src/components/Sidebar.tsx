import kyokushinLogo from "../assets/Kyokushin Karate Logo Vector.svg";
import BeltBadge from "./BeltBadge";
import type { CurrentUser } from "../App";

const beltDescriptions = [
  { color: "white", rank: "10th kyu", meaning: "Purity and potential" },
  { color: "orange", rank: "10th & 9th kyu", meaning: "Stability and basic conditioning" },
  { color: "blue", rank: "8th & 7th kyu", meaning: "Fluidity and adaptability" },
  { color: "yellow", rank: "6th & 5th kyu", meaning: "Power and body awareness" },
  { color: "green", rank: "4th & 3rd kyu", meaning: "Emotion and sensitivity" },
  { color: "brown", rank: "2nd & 1st kyu", meaning: "Practical application and creativity" },
  { color: "black", rank: "1st – 10th dan", meaning: "Maturity and mastery" },
];

export default function Sidebar({ user }: { user: CurrentUser | null }) {
  return (
    <aside
      className="w-64 flex-shrink-0 sticky top-0 self-start min-h-screen text-slate-900 flex flex-col p-4 overflow-y-auto"
      style={{
        background: "linear-gradient(135deg, #e6e8eb 0%, #788ca0 35%, #959597 65%, #f0f2f5 100%)",
      }}
    >
      <div className="mb-6">
        <div className="flex items-center gap-3 mb-3">
          <img
            src={kyokushinLogo}
            alt="Kyokushin logo"
            className="h-10 w-10 rounded-full bg-slate-300 object-contain"
          />
          <h1 className="text-2xl font-bold text-black">Ronin</h1>
        </div>
        {user ? (
          <div className="flex items-center gap-2 text-sm text-slate-800">
            <BeltBadge color={user.rank.beltColor} size="sm" />
            <span>{user.rank.name}</span>
          </div>
        ) : null}
      </div>

      <blockquote
        className="mb-6 rounded-2xl border border-slate-400 p-4 text-sm italic text-slate-900"
        style={{
          background: "linear-gradient(135deg, #e6e8eb 0%, #788ca0 35%, #959597 65%, #f0f2f5 100%)",
        }}
      >
        It’s not just about fighting for your life; it's a path toward self-discovery and absolute honesty with oneself through hard work and discipline.
      </blockquote>

      <section
        className="mb-6 rounded-2xl border border-slate-400 p-4 text-sm text-slate-900"
        style={{
          background: "linear-gradient(135deg, #e6e8eb 0%, #788ca0 35%, #959597 65%, #f0f2f5 100%)",
        }}
      >
        <div className="mb-3 text-base font-semibold text-slate-950">About Ronin rank</div>
        <ul className="space-y-3">
          {beltDescriptions.map((belt) => (
            <li key={belt.rank} className="flex items-start gap-3">
              <div className="mt-1 flex-shrink-0">
                <BeltBadge color={belt.color} size="sm" />
              </div>
              <div>
                <div className="font-semibold text-slate-950">{belt.rank}</div>
                <div className="text-slate-800">{belt.meaning}</div>
              </div>
            </li>
          ))}
        </ul>
      </section>

    </aside>
  );
}
