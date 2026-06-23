import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApp } from '../../context/AppContext';
import {
  LogIn,
  UserPlus,
  AlertCircle,
  Mail,
  Lock,
  User,
  Phone,
} from 'lucide-react';
import { toast } from 'sonner@2.0.3';
import { DEMO_CUSTOMERS } from '../../data/users';
import { Logo } from '../common/Logo';

export default function LoginPage() {
  const [isLogin, setIsLogin] = useState(true);

  // common
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  // register-only
  const [username, setUsername] = useState('');
  const [phone, setPhone] = useState('');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');

  const navigate = useNavigate();
  const { login, register } = useApp();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (isLogin) {
      const foundCustomer = DEMO_CUSTOMERS.find(
        (c) => c.email === email && c.password === password
      );

      login(email, password);
      toast.success(
        foundCustomer
          ? `Chào mừng trở lại, ${foundCustomer.name}!`
          : 'Đăng nhập thành công!'
      );
      navigate('/');
    } else {
      const payload = {
        username,
        phone,
        email,
        firstName,
        lastName,
        password,
      };

      register(payload);
      toast.success('Đăng ký thành công!');
      navigate('/');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-gradient-to-br from-[#FEFFAF]/40 to-[#78A2D2]/30">
      <div className="bg-white rounded-3xl shadow-2xl p-8 w-full max-w-md">
        {/* Logo */}
        <div className="flex justify-center mb-8">
          <Logo size="default" />
        </div>

        {/* Switch */}
        <div className="flex gap-2 mb-6">
          <button
            onClick={() => setIsLogin(true)}
            className={`flex-1 py-3 rounded-xl font-semibold transition-all ${
              isLogin
                ? 'bg-[#78A2D2] text-white shadow-md'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            <LogIn className="inline size-5 mr-2" />
            Đăng Nhập
          </button>
          <button
            onClick={() => setIsLogin(false)}
            className={`flex-1 py-3 rounded-xl font-semibold transition-all ${
              !isLogin
                ? 'bg-[#78A2D2] text-white shadow-md'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            <UserPlus className="inline size-5 mr-2" />
            Đăng Ký
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-4">
          {!isLogin && (
            <>
              {/* Username */}
              <Input
                icon={<User />}
                placeholder="Username"
                value={username}
                onChange={setUsername}
              />

              {/* Name */}
              <div className="grid grid-cols-2 gap-3">
                <Input
                  placeholder="Họ"
                  value={lastName}
                  onChange={setLastName}
                />
                <Input
                  placeholder="Tên"
                  value={firstName}
                  onChange={setFirstName}
                />
              </div>

              {/* Phone */}
              <Input
                icon={<Phone />}
                placeholder="Số điện thoại"
                value={phone}
                onChange={setPhone}
              />
            </>
          )}

          {/* Email */}
          <Input
            icon={<Mail />}
            type="email"
            placeholder="Email"
            value={email}
            onChange={setEmail}
          />

          {/* Password */}
          <Input
            icon={<Lock />}
            type="password"
            placeholder="Mật khẩu"
            value={password}
            onChange={setPassword}
          />

          <button
            type="submit"
            className="
              w-full py-3 rounded-xl font-semibold
              bg-[#FEFFAF]
              border-2 border-[#78A2D2]
              text-[#2C2C2C]
              shadow-md
              hover:shadow-lg hover:-translate-y-[1px]
              active:translate-y-0
              transition-all
            "
          >
            {isLogin ? 'Đăng Nhập' : 'Đăng Ký'}
          </button>
        </form>

        {/* Divider */}
        <div className="relative my-6">
          <div className="absolute inset-0 flex items-center">
            <div className="w-full border-t border-gray-300"></div>
          </div>
          <div className="relative flex justify-center text-sm">
            <span className="px-4 bg-white text-gray-500 font-medium">
              Hoặc
            </span>
          </div>
        </div>

        {/* Google */}
        <button
          type="button"
          onClick={() =>
            toast.info('Tính năng đăng nhập Google đang được phát triển')
          }
          className="
            w-full flex items-center justify-center gap-3
            bg-white border-2 border-gray-300
            text-gray-700 py-3 rounded-xl
            hover:bg-gray-50 hover:border-gray-400
            transition-all shadow-md font-semibold
          "
        >
          <svg className="size-5" viewBox="0 0 24 24">
            <path
              d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
              fill="#4285F4"
            />
            <path
              d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
              fill="#34A853"
            />
            <path
              d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
              fill="#FBBC05"
            />
            <path
              d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
              fill="#EA4335"
            />
          </svg>
          Đăng nhập với Google
        </button>

        {/* Demo */}
        {isLogin && (
          <div className="mt-6 pt-6 border-t border-gray-200">
            <p className="text-xs font-semibold text-gray-700 mb-3 flex items-center gap-1">
              <AlertCircle className="size-3" />
              Tài khoản demo khách hàng:
            </p>
            <div className="bg-[#FEFFAF]/40 p-3 rounded-lg border border-[#78A2D2]/30">
              <p className="text-xs font-medium">👑 Thành viên Vàng</p>
              <p className="text-xs font-mono text-[#78A2D2]">
                customer@kinderland.vn / customer123
              </p>
              <p className="text-xs text-gray-600 mt-1">
                Nguyễn Thị Lan – 1,500 điểm
              </p>
            </div>
          </div>
        )}

        <div className="mt-4 text-center">
          <button
            type="button"
            onClick={() => navigate('/admin/login')}
            className="text-xs text-[#78A2D2] hover:underline font-medium"
          >
            Đăng nhập dành cho Admin/Nhân viên →
          </button>
        </div>
      </div>
    </div>
  );
}

/* ===================== */
/* Reusable Input */
/* ===================== */

function Input({
  icon,
  type = 'text',
  placeholder,
  value,
  onChange,
}: {
  icon?: React.ReactNode;
  type?: string;
  placeholder: string;
  value: string;
  onChange: (v: string) => void;
}) {
  return (
    <div className="relative">
      {icon && (
        <span className="
          absolute left-4 top-1/2 -translate-y-1/2
          text-gray-400
          pointer-events-none
        ">
          {/* ép size icon */}
          {React.isValidElement(icon)
            ? React.cloneElement(icon, { size: 18 })
            : icon}
        </span>
      )}

      <input
        type={type}
        placeholder={placeholder}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        required
        className={`
          w-full px-4 py-3 rounded-xl text-sm
          border border-gray-300
          placeholder-gray-400
          shadow-sm
          transition-all
          focus:outline-none focus:border-[#78A2D2]
          focus:ring-2 focus:ring-[#78A2D2]/30
          hover:border-gray-400
          ${icon ? 'pl-12' : ''}
        `}
      />
    </div>
  );
}

