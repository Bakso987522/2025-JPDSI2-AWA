import {useAuthStore} from "@/store/useAuthStore";
import {router} from "next/client";

const BASE_URL = '';

interface RequestConfig extends RequestInit {
    data?: any;
}

export async function client<T>(endpoint: string, { data, headers: customHeaders, ...customConfig }: RequestConfig = {}): Promise<T> {

    const config: RequestInit = {
        method: data ? "POST" : "GET",
        body: data ? JSON.stringify(data) : undefined,
        headers: {
            "Content-Type": "application/json",
            ...customHeaders,
        },
        ...customConfig,
    };

    const response = await window.fetch(`${BASE_URL}${endpoint}`, config);

    if (response.status === 401) {
        return Promise.reject({ message: "Sesja wygasła, zaloguj się ponownie." });
        useAuthStore.getState().logout();
        router.push('/login');
    }

    if (!response.ok) {
        const error = await response.json().catch(() => ({ error: "Błąd serwera" }));
        throw new Error(error.error || error.message || "Wystąpił błąd");
    }

    const text = await response.text();
    return text ? JSON.parse(text) : ({} as T);
}