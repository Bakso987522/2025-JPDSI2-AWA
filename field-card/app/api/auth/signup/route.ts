// app/api/auth/register/route.ts
import { NextResponse } from "next/server";

export async function POST(request: Request) {
    const body = await request.json();
    const backendUrl = process.env.SPRING_API_URL || 'http://localhost:8080/api';

    try {
        const res = await fetch(`${backendUrl}/auth/register`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body),
        });

        if (!res.ok) {
            return NextResponse.json(
                { message: "Błąd rejestracji w backendzie" },
                { status: res.status }
            );
        }

        const data = await res.json();
        const token = data.token;

        const response = NextResponse.json({ success: true, token: token });

        response.cookies.set({
            name: "token",
            value: token,
            httpOnly: true,
            secure: process.env.NODE_ENV === "production",
            sameSite: "strict",
            path: "/",
            maxAge: 60 * 60 * 24,
        });

        return response;

    } catch (error) {
        return NextResponse.json({ message: "Serwer nie odpowiada" }, { status: 500 });
    }
}