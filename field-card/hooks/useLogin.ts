import { useMutation } from "@tanstack/react-query";
import { useAuthStore } from "@/store/useAuthStore";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";

const loginSchema = z.object({
    email: z.string().email("Nieprawidłowy email"),
    password: z.string().min(6, "Hasło musi mieć min. 6 znaków"),
});

type LoginFormValues = z.infer<typeof loginSchema>;

export const useLogin = () => {
    const { login } = useAuthStore();
    const router = useRouter();

    const form = useForm<LoginFormValues>({
        resolver: zodResolver(loginSchema),
        defaultValues: { email: "", password: "" },
    });

    const mutation = useMutation({
        mutationFn: async (data: LoginFormValues) => {
            const res = await fetch("/api/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(data),
            });

            if (!res.ok) {
                const err = await res.json();
                throw new Error(err.error || "Błąd logowania");
            }
            return res.json();
        },
        onSuccess: (data, variables) => {
            login({ email: variables.email, name: data.name });
            router.push("/dashboard");
        },
        onError: (error) => {
            form.setError("root", { message: error.message });
        },
    });

    return {
        form,
        onSubmit: (data: LoginFormValues) => mutation.mutate(data),
        isPending: mutation.isPending,
    };
};