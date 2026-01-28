import { useMutation } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/store/useAuthStore";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";

const signupSchema = z.object({
    name: z.string().min(2, "Imię musi mieć co najmniej 2 znaki"),
    email: z.string().email("Nieprawidłowy adres email"),
    password: z.string().min(6, "Hasło musi mieć co najmniej 6 znaków"),
    confirmPassword: z.string()
}).refine((data) => data.password === data.confirmPassword, {
    message: "Hasła muszą być identyczne",
    path: ["confirmPassword"],
});

export type SignupFormValues = z.infer<typeof signupSchema>;

export const useSignup = () => {
    const router = useRouter();
    const { login } = useAuthStore();

    const form = useForm<SignupFormValues>({
        resolver: zodResolver(signupSchema),
        defaultValues: {
            name: "",
            email: "",
            password: "",
            confirmPassword: ""
        },
    });

    const mutation = useMutation({
        mutationFn: async (data: SignupFormValues) => {
            const res = await fetch("/api/auth/signup", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    name: data.name,
                    email: data.email,
                    password: data.password
                }),
            });

            if (!res.ok) {
                const errorData = await res.json().catch(() => ({}));
                throw new Error(errorData.error || "Błąd rejestracji");
            }
            return res.json();
        },
        onSuccess: (data, variables) => {
            login({
                email: variables.email,
                name: variables.name
            });

            router.push("/dashboard");
        },
        onError: (error) => {
            form.setError("root", { message: error.message });
        },
    });

    return {
        form,
        onSubmit: form.handleSubmit((data) => mutation.mutate(data)),
        isPending: mutation.isPending,
    };
};