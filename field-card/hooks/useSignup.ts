import { useMutation } from '@tanstack/react-query';
import { useRouter } from 'next/navigation';
import { z } from 'zod';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useAuthStore } from '@/store/useAuthStore';

const signupSchema = z.object({
    name: z.string().min(2, "Imię musi mieć co najmniej 2 znaki"),
    email: z.string().email("Niepoprawny format adresu email"),
    password: z.string().min(6, "Hasło musi mieć co najmniej 6 znaków"),
    confirmPassword: z.string()
}).refine((data) => data.password === data.confirmPassword, {
    message: "Hasła muszą być identyczne",
    path: ["confirmPassword"],
});

export type SignupFormValues = z.infer<typeof signupSchema>;

export const useSignup = () => {
    const router = useRouter();
    const setToken = useAuthStore((state) => state.setToken);

    const form = useForm<SignupFormValues>({
        resolver: zodResolver(signupSchema),
        defaultValues: { name: '', email: '', password: '', confirmPassword: '' },
    });

    const mutation = useMutation({
        mutationFn: async (data: SignupFormValues) => {
            const { confirmPassword, ...requestData } = data;

            const response = await fetch('/api/auth/signup', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(requestData),
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || 'Błąd rejestracji');
            }

            return response.json();
        },
        onSuccess: (data) => {
            setToken(data.token || "logged-in");
            router.push('/dashboard');
        },
        onError: (error: any) => {
            console.error("Błąd rejestracji:", error);
            form.setError('root', { message: error.message || "Rejestracja nieudana." });
        },
    });

    const onSubmit = (data: SignupFormValues) => {
        mutation.mutate(data);
    };

    return { form, onSubmit, isPending: mutation.isPending };
};