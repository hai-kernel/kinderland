# AI Coding Agent Instructions — Kinderland Toy Store

These notes make an AI immediately productive in this repo. Keep guidance concrete and codebase-specific.

## Overview
- Stack: Vite + React 18 + TypeScript, Tailwind (prebuilt CSS), Radix UI primitives, shadcn-style wrappers, Recharts.
- App shape: Customer storefront + Admin + Staff. Routing via React Router in [src/App.tsx](src/App.tsx).
- State: Customer/cart/voucher in `useApp()` ([src/context/AppContext.tsx](src/context/AppContext.tsx)); Admin/Staff auth in `useAdmin()` with localStorage persistence ([src/context/AdminContext.tsx](src/context/AdminContext.tsx)).
- Data: Mocked in [src/data](src/data) (products, users, blogs, stores). No backend; checkout/payment is simulated.

## Run & Build
- Install/start:
  - `npm i`
  - `npm run dev` (Vite dev server opens on port 3000 per [vite.config.ts](vite.config.ts)).
- Build: `npm run build` → outputs to `build/` (not `dist/`).
- Module alias: `@` resolves to `./src` (see `resolve.alias` in [vite.config.ts](vite.config.ts)).

## Routing & Access Control
- Customer routes are nested under the catch-all `/*` with shared [components/layout/Navbar.tsx](src/components/layout/Navbar.tsx) and [components/layout/Footer.tsx](src/components/layout/Footer.tsx).
- Auth gates:
  - `ProtectedRoute` (customer): requires `useApp().user` for `/checkout`, `/payment`, `/order-success`, `/account/*`.
  - `AdminProtectedRoute` (staff/admin): checks `useAdmin().adminUser.role` for `/admin/*` (admin) and `/staff/*` (staff).
- When adding routes, mirror the existing split: public pages in [src/components/pages](src/components/pages), staff/admin tools in [src/components/staff](src/components/staff) and [src/components/admin](src/components/admin).

## UI System & Styling
- Use the local UI primitives in [src/components/ui](src/components/ui) (e.g., `Button`, `Input`, `Card`). They wrap Radix + class-variance-authority with a shared `cn()` util ([src/components/ui/utils.ts](src/components/ui/utils.ts)).
- Styling tokens live in [src/styles/globals.css](src/styles/globals.css) (Kinderland theme). Prefer CSS variables (e.g., `bg-background`, `text-foreground`) over hard-coded colors.
- Tailwind output is precompiled into [src/index.css](src/index.css); you typically only add utility classes in TSX.
- Toasts/chrome: `sonner` toasts are used; icons from `lucide-react`.

## Import Conventions (important)
- Several packages are imported with version-suffixed specifiers that map via Vite aliases. Follow existing patterns to avoid resolution errors:
  - `import { toast } from 'sonner@2.0.3'`
  - `import { Slot } from '@radix-ui/react-slot@1.1.2'`
  - `import { cva } from 'class-variance-authority@0.7.1'`
- For paths, prefer alias imports from `@/…` (e.g., `@/components/ui/button`).

## Data & Domain
- Products shape: see `Product` in [src/data/products.ts](src/data/products.ts). Many components assume fields like `types`, `discount`, `rating` exist; populate them when adding products.
- Demo accounts:
  - Customers in [src/data/users.ts](src/data/users.ts) and documented in [src/DEMO_ACCOUNTS.md](src/DEMO_ACCOUNTS.md).
  - Admin/Staff users are in-component mocks in [src/components/admin/AdminLogin.tsx](src/components/admin/AdminLogin.tsx).
- Vouchers are hardcoded in `AppContext` (`GIAM10`, `GIAM50K`, `FREESHIP`); `applyVoucher(code)` updates context state.
- Currency/locale: format with `Intl.NumberFormat('vi-VN', { currency: 'VND' })` (see [src/components/checkout/Payment.tsx](src/components/checkout/Payment.tsx)).

## Patterns to Reuse
- Auth flows: use `useApp().login()` for customers; `useAdmin().loginAdmin()` for admin/staff. Respect role redirects shown in [src/App.tsx](src/App.tsx).
- Page composition: pages live under [src/components/pages](src/components/pages); product detail under [src/components/shop](src/components/shop); cart/checkout/payment under [src/components/checkout](src/components/checkout).
- Theming: prefer semantic classes and tokens from `globals.css`; see color migration notes in [src/COLOR_MIGRATION_MAP.md](src/COLOR_MIGRATION_MAP.md) when adjusting styles.

## Practical Examples
- Add a toast: `import { toast } from 'sonner@2.0.3'; toast.success('Đã lưu!')`.
- Use a UI button: `import { Button } from '@/components/ui/button';` then `<Button variant="secondary">Mua ngay</Button>`.
- Add a protected staff tool: create component under [src/components/staff](src/components/staff), then add a route within `AdminProtectedRoute` with `allowedRoles={['staff']}` in [src/App.tsx](src/App.tsx).

If anything above is unclear (e.g., import specifiers or where to register new routes/components), tell us which task you’re attempting and we’ll clarify or extend these notes.