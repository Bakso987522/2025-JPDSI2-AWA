import { ProductDetailsSchema, type ProductDetails } from "@/lib/schemas/product";
import { cookies } from "next/headers";

const SPRING_API_URL = process.env.SPRING_API_URL || "http://localhost:8080/api";

export const productService = {
    getProductById: async (id: string): Promise<ProductDetails | null> => {
        try {
            const cookieStore = await cookies();
            const token = cookieStore.get("token")?.value;

            const headers: HeadersInit = {
                "Content-Type": "application/json",
            };

            if (token) {
                headers["Authorization"] = `Bearer ${token}`;
            }

            const res = await fetch(`${SPRING_API_URL}/products/${id}`, {
                cache: "no-store",
                headers: headers
            });

            if (!res.ok) {
                console.error(`API Error: ${res.status} ${res.statusText} dla ID: ${id}`);
                if (res.status === 401 || res.status === 403) return null;
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