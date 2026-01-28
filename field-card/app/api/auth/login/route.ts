import { NextRequest, NextResponse } from "next/server";
import { cookies } from "next/headers";

const SPRING_API_URL = process.env.SPRING_API_URL || "http://localhost:8080/api";

export async function POST(req: NextRequest) {
    try {
        const body = await req.json();

        const springRes = await fetch(`${SPRING_API_URL}/auth/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body),
        });

        if (!springRes.ok) {
            const errorData = await springRes.json().catch(() => ({}));
            return NextResponse.json(
                { error: errorData.message || "Błąd logowania" },
                { status: springRes.status }
            );
        }

        const data = await springRes.json();
        const token = data.token;

        if (!token) {
            return NextResponse.json({ error: "Brak tokena w odpowiedzi" }, { status: 500 });
        }

        const cookieStore = await cookies();
        cookieStore.set("token", token, {
            httpOnly: true,
            secure: process.env.NODE_ENV === "production",
            sameSite: "strict",
            path: "/",
            maxAge: 60 * 60 * 24 * 7,
        });

        return NextResponse.json({
            success: true,
            user: { email: body.email },
            name: data.name
        });

    } catch (error) {
        console.error("Login BFF Error:", error);
        return NextResponse.json({ error: "Błąd serwera" }, { status: 500 });
    }
}