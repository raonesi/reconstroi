import { AppShell } from "@/components/app-shell";
export default function PrivateLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return <AppShell>{children}</AppShell>;
}
