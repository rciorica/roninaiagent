import type { ReactNode } from "react";
import Sidebar from "../components/Sidebar";
import Header from "../components/Header";
import type { CurrentUser } from "../App";

export default function MainLayout({
  children,
  userEmail,
  user,
  onLogout,
}: {
  children: ReactNode;
  userEmail: string | null;
  user: CurrentUser | null;
  onLogout: () => void;
}) {
  return (
    <div className="min-h-screen flex min-w-0">
      <div className="hidden lg:flex">
        <Sidebar user={user} />
      </div>

      <div className="flex-1 min-h-0 min-w-0 flex flex-col">
        <Header userEmail={userEmail} user={user} onLogout={onLogout} />
        <main
          className="flex-1 min-h-0 min-w-0 overflow-y-auto overflow-x-hidden p-2 sm:p-4 lg:p-6"
          style={{
            background: "linear-gradient(135deg, #e6e8eb 0%, #788ca0 35%, #959597 65%, #f0f2f5 100%)",
          }}
        >
          {children}
        </main>
      </div>
    </div>
  );
}
