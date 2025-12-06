import { NextRequest, NextResponse } from "next/server";
import { cookies } from "next/headers";

const SPRING_API_URL = process.env.SPRING_API_URL || "http://localhost:8080/api";

async function handler(req: NextRequest, { params }: { params: Promise<{ slug: string[] }> }) {
    const resolvedParams = await params;

    const pathArray = resolvedParams.slug;

    const path = pathArray.join("/");
    const query = req.nextUrl.search;
    const targetUrl = `${SPRING_API_URL}/${path}${query}`;

    const cookieStore = await cookies();
    const token = cookieStore.get("token")?.value;

    const headers: HeadersInit = {
        "Content-Type": "application/json",
    };

    if (token) {
        headers["Authorization"] = `Bearer ${token}`;
    }

    let body = null;
    if (req.method !== "GET" && req.method !== "HEAD") {
        try {
            body = JSON.stringify(await req.json());
        } catch (e) {}
    }

    try {
        const springResponse = await fetch(targetUrl, {
            method: req.method,
            headers,
            body,
            cache: "no-store",
        });

        let data = {};
        const contentType = springResponse.headers.get("content-type");
        if (contentType && contentType.includes("application/json")) {
            data = await springResponse.json();
        }

        return NextResponse.json(data, { status: springResponse.status });

    } catch (error) {
        console.error("Błąd tunelu:", error);
        return NextResponse.json({ error: "Błąd komunikacji z backendem" }, { status: 500 });
    }
}

export { handler as GET, handler as POST, handler as PUT, handler as DELETE, handler as PATCH };