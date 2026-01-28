import {
    LayoutDashboard,
    Tractor,
    Sprout,
    BookOpen,
    FlaskConical,
    Bug,
    Settings2,
    Calendar,
    FileText,
    CloudSun,
    Leaf,
    ShieldAlert
} from "lucide-react";

const sidebarData = {
    logo: Sprout,
    user: {
        name: "",
        email: "",
        avatar: "",
    },
    navMain: [
        {
            title: "Pulpit",
            url: "/dashboard",
            icon: LayoutDashboard,
            isActive: true,
        },
        {
            title: "Moje Pola",
            url: "/dashboard/fields",
            icon: Tractor,
        },
        {
            title: "Inwentaryzacja",
            url: "/dashboard/inventory",
            icon: FlaskConical,
        },
        {
            title: "Dziennik Polowy",
            url: "/dashboard/journal",
            icon: Calendar,
        },
        {
            title: "Vademecum",
            url: "/dashboard/vademecum",
            icon: BookOpen,
            isActive: true,
        },
    ],
    // projects: [
    //     {
    //         name: "Komunikaty PIORiN",
    //         url: "https://piorin.gov.pl/",
    //         icon: ShieldAlert,
    //     },
    //     {
    //         name: "Pogoda dla rolnika",
    //         url: "/dashboard/weather",
    //         icon: CloudSun,
    //     },
    //     {
    //         name: "Etykiety ŚOR",
    //         url: "https://www.gov.pl/web/rolnictwo/wyszukiwarka-srodkow-ochrony-roslin",
    //         icon: FileText,
    //     },
    // ],
    navSecondary: [
        // {
        //     title: "Ustawienia Gospodarstwa",
        //     url: "/dashboard/settings",
        //     icon: Settings2,
        // },
        // {
        //     title: "Pomoc",
        //     url: "/help",
        //     icon: Leaf,
        // },
    ],
}

export default sidebarData;