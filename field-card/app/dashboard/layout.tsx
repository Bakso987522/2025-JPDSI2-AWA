import {SidebarInset, SidebarProvider} from "@/components/ui/sidebar";
import {AppSidebar} from "@/components/app-sidebar";
import {Toaster} from "@/components/ui/sonner";

export default function DashboardLayout({
                                       children,
                                   }: Readonly<{
    children: React.ReactNode;
}>) {
    return (
        <SidebarProvider>
            <AppSidebar />
            <SidebarInset>
                <Toaster/>
                {children}
            </SidebarInset>
        </SidebarProvider>
    );
}