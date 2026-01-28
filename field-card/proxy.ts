import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'

export default function proxy(request: NextRequest) {
    const token = request.cookies.get('token')?.value
    const { pathname } = request.nextUrl
    const isProtectedRoute = pathname.startsWith('/dashboard')
    const isAuthRoute = pathname.startsWith('/login') || pathname.startsWith('/signup')
    if (isProtectedRoute && !token) {
        const loginUrl = new URL('/login', request.url)
        loginUrl.searchParams.set('callbackUrl', pathname)
        return NextResponse.redirect(loginUrl)
    }

    if (isAuthRoute && token) {
        return NextResponse.redirect(new URL('/dashboard', request.url))
    }

    return NextResponse.next()
}

export const config = {
    matcher: [
        '/((?!api|_next/static|_next/image|favicon.ico|.*\\.svg|.*\\.png).*)',
    ],
}