import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface User {
    email: string;
    name?: string;
}

interface AuthState {
    user: User | null;
    isAuthenticated: boolean;
    login: (user: User) => void;
    logout: () => void;
    name: string;
}

export const useAuthStore = create<AuthState>()(
    persist(
        (set) => ({
            user: null,
            isAuthenticated: false,
            login: (user) => set({ user, isAuthenticated: true }),
            logout: () => set({ user: null, isAuthenticated: false }),
            name: "",
        }),
        {
            name: 'fieldcard-user-storage',
        }
    )
);