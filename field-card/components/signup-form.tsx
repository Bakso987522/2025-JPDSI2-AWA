"use client"

import { Button } from "@/components/ui/button"
import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "@/components/ui/card"
import {
    Field,
    FieldDescription,
    FieldError,
    FieldGroup,
    FieldLabel,
} from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import { useSignup } from "@/hooks/useSignup"

export function SignupForm({ ...props }: React.ComponentProps<typeof Card>) {
    const { form, onSubmit, isPending } = useSignup()
    const { register, handleSubmit, formState: { errors } } = form

    return (
        <Card {...props}>
            <CardHeader>
                <CardTitle>Create an account</CardTitle>
                <CardDescription>
                    Enter your information below to create your account
                </CardDescription>
            </CardHeader>
            <CardContent>
                <form onSubmit={handleSubmit(onSubmit)}>
                    <FieldGroup>
                        <Field>
                            <FieldLabel htmlFor="name">Full Name</FieldLabel>
                            <Input
                                id="name"
                                type="text"
                                placeholder="John Doe"
                                {...register("name")}
                            />
                            <FieldError errors={[errors.name]} />
                        </Field>

                        <Field>
                            <FieldLabel htmlFor="email">Email</FieldLabel>
                            <Input
                                id="email"
                                type="email"
                                placeholder="m@example.com"
                                {...register("email")}
                            />
                            <FieldDescription>
                                We&apos;ll use this to contact you.
                            </FieldDescription>
                            <FieldError errors={[errors.email]} />
                        </Field>

                        <Field>
                            <FieldLabel htmlFor="password">Password</FieldLabel>
                            <Input
                                id="password"
                                type="password"
                                {...register("password")}
                            />
                            <FieldDescription>
                                Must be at least 6 characters long.
                            </FieldDescription>
                            <FieldError errors={[errors.password]} />
                        </Field>

                        <Field>
                            <FieldLabel htmlFor="confirm-password">
                                Confirm Password
                            </FieldLabel>
                            <Input
                                id="confirm-password"
                                type="password"
                                {...register("confirmPassword")}
                            />
                            <FieldError errors={[errors.confirmPassword]} />
                            <FieldError errors={[errors.root]} />
                        </Field>

                        <FieldGroup>
                            <Field>
                                <Button type="submit" disabled={isPending}>
                                    {isPending ? "Creating account..." : "Create Account"}
                                </Button>
                                <Button variant="outline" type="button">
                                    Sign up with Google
                                </Button>
                                <FieldDescription className="px-6 text-center">
                                    Already have an account? <a href="/login">Sign in</a>
                                </FieldDescription>
                            </Field>
                        </FieldGroup>
                    </FieldGroup>
                </form>
            </CardContent>
        </Card>
    )
}