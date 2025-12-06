import { useMutation } from '@tanstack/react-query';
import { useRouter } from 'next/navigation';
import { z } from 'zod';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useAuthStore } from '@/store/useAuthStore';

const loginSchema = z.object({
    email: z.string().email("Niepoprawny format adresu email"),
    password: z.string().min(6, "Hasło musi mieć co najmniej 6 znaków"),
});

export type LoginFormValues = z.infer<typeof loginSchema>;

export const useLogin = () => {
    const router = useRouter();
    const setToken = useAuthStore((state) => state.setToken);

    const form = useForm<LoginFormValues>({
        resolver: zodResolver(loginSchema),
        defaultValues: { email: '', password: '' },
    });

    const mutation = useMutation({
        mutationFn: async (data: LoginFormValues) => {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data),
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || 'Błąd logowania');
            }

            return response.json();
        },
        onSuccess: (data) => {
            setToken(data.token || "logged-in");
            router.push('/dashboard');
        },
        onError: (error: any) => {
            console.error("Błąd logowania:", error);
            form.setError('root', { message: error.message || "Nieprawidłowy email lub hasło" });
        },
    });

    const onSubmit = (data: LoginFormValues) => {
        mutation.mutate(data);
    };

    return { form, onSubmit, isPending: mutation.isPending };
};