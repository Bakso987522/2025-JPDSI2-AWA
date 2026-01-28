import { useMutation } from "@tanstack/react-query";
import { useAuthStore } from "@/store/useAuthStore";
import { useRouter } from "next/navigation";

export const useLogout = () => {
    const { logout: logoutStore } = useAuthStore();
    const router = useRouter();

    const mutation = useMutation({
        mutationFn: async () => {
            await fetch("/api/auth/logout", { method: "POST" });
        },
        onSuccess: () => {
            logoutStore();
            router.push("/login");
        },
    });

    return {
        logout: mutation.mutate,
        isLoggingOut: mutation.isPending
    };
};