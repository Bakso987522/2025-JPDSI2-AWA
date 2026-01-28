"use client"

import * as React from "react"
import {
    Command
} from "lucide-react"

import { NavMain } from "@/components/nav-main"
import { NavProjects } from "@/components/nav-projects"
import { NavSecondary } from "@/components/nav-secondary"
import { NavUser } from "@/components/nav-user"
import {
    Sidebar,
    SidebarContent,
    SidebarFooter,
    SidebarHeader,
    SidebarMenu,
    SidebarMenuButton,
    SidebarMenuItem,
} from "@/components/ui/sidebar"
import sidebarData from "@/config/sidebar"
import { useAuthStore } from "@/store/useAuthStore" //

export function AppSidebar({ ...props }: React.ComponentProps<typeof Sidebar>) {
    const userFromStore = useAuthStore((state) => state.user);

    const activeUser = {
        name: userFromStore?.name || "Użytkownik",
        email: userFromStore?.email || "brak@email.com",
        avatar: "",
    }

    const data = sidebarData;

    return (
        <Sidebar variant="inset" {...props}>
            <SidebarHeader>
                <SidebarMenu>
                    <SidebarMenuItem>
                        <SidebarMenuButton size="lg" asChild>
                            <a href="#">
                                <div className="bg-sidebar-primary text-sidebar-primary-foreground flex aspect-square size-8 items-center justify-center rounded-lg">
                                    <data.logo className="size-4" />
                                </div>
                                <div className="grid flex-1 text-left text-sm leading-tight">
                                    <span className="truncate font-medium">FieldCard</span>
                                    <span className="truncate text-xs">System zarządzania</span>
                                </div>
                            </a>
                        </SidebarMenuButton>
                    </SidebarMenuItem>
                </SidebarMenu>
            </SidebarHeader>

            <SidebarContent>
                <NavMain items={data.navMain} />

                <NavSecondary items={data.navSecondary} className="mt-auto" />
            </SidebarContent>

            <SidebarFooter>
                <NavUser user={activeUser} />
            </SidebarFooter>
        </Sidebar>
    )
}