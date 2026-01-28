import { NextRequest, NextResponse } from "next/server";
import { cookies } from "next/headers";

const SPRING_API_URL = process.env.SPRING_API_URL || "http://localhost:8080/api";

export async function POST(req: NextRequest) {
    try {
        const body = await req.json();

        const springRes = await fetch(`${SPRING_API_URL}/auth/register`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body),
        });

        if (!springRes.ok) {
            const errorData = await springRes.json().catch(() => ({}));
            return NextResponse.json(
                { error: errorData.message || "Błąd rejestracji" },
                { status: springRes.status }
            );
        }

        const data = await springRes.json();
        const token = data.token;

        if (token) {
            const cookieStore = await cookies();
            cookieStore.set("token", token, {
                httpOnly: true,
                secure: process.env.NODE_ENV === "production",
                sameSite: "strict",
                path: "/",
            });
        }

        return NextResponse.json({ success: true });

    } catch (error) {
        return NextResponse.json({ error: "Błąd serwera" }, { status: 500 });
    }
}