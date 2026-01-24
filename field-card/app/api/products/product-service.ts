import { ProductDetailsSchema, type ProductDetails } from "@/lib/schemas/product";
import { cookies } from "next/headers";

const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api";

export const productService = {
    getProductById: async (id: string): Promise<ProductDetails | null> => {
        try {
            const cookieStore = await cookies();

            const cookieHeader = cookieStore.getAll()
                .map(c => `${c.name}=${c.value}`)
                .join('; ');

            const res = await fetch(`${API_URL}/products/${id}`, {
                cache: "no-store",
                headers: {
                    "Cookie": cookieHeader,
                    "Content-Type": "application/json"
                }
            });

            if (!res.ok) {
                console.error(`API Error: ${res.status} ${res.statusText} dla ID: ${id}`);
                if (res.status === 403) return null;
                return null;
            }

            const rawData = await res.json();

            const parsed = ProductDetailsSchema.safeParse(rawData);

            if (!parsed.success) {
                console.error("Błąd walidacji Zod:", parsed.error);
                return null;
            }

            return parsed.data;
        } catch (error) {
            console.error("Błąd sieci:", error);
            return null;
        }
    }
};