
import { useState } from "react";
import { useSearchParams, useNavigate } from "react-router-dom";
import axios from "axios";

const ResetPassword = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const token = searchParams.get("token");

  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [message, setMessage] = useState("");
  const [isSuccess, setIsSuccess] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage("");
    setIsSuccess(false);

    if (!token) {
      setMessage("Invalid or missing reset token.");
      return;
    }

    if (password !== confirmPassword) {
      setMessage("Passwords do not match.");
      return;
    }

    if (password.length < 8) {
      setMessage("Password must contain at least 8 characters.");
      return;
    }

    try {
      setLoading(true);

      const response = await axios.post(
        "http://localhost:5054/reset-password",
        { token, password }
      );

      setMessage(
        response.data.message || "Password updated successfully."
      );
      setIsSuccess(true);

      setTimeout(() => navigate("/"), 2000);

    } catch (error) {
      setMessage(
        error.response?.data?.error ||
        error.response?.data?.message ||
        "Unable to reset password. Please try again."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-950 px-4">
      <form
        onSubmit={handleSubmit}
        className="bg-white p-8 rounded-lg shadow-lg w-full max-w-md space-y-5"
      >
        <h1 className="text-2xl font-semibold text-center text-gray-900">
          Reset Password
        </h1>

        <input
          type="password"
          placeholder="Enter new password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          className="w-full border border-gray-400 rounded-md p-3 text-gray-900 placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-slate-500"
          required
        />

        <input
          type="password"
          placeholder="Confirm new password"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
          className="w-full border border-gray-400 rounded-md p-3 text-gray-900 placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-slate-500"
          required
        />

        {message && (
          <p
            className={`text-sm text-center ${
              isSuccess ? "text-green-600" : "text-red-600"
            }`}
          >
            {message}
          </p>
        )}

        <button
          type="submit"
          disabled={loading}
          className="w-full bg-slate-800 hover:bg-slate-700 text-white rounded-md p-3 disabled:opacity-60"
        >
          {loading ? "Updating..." : "Reset Password"}
        </button>
      </form>
    </div>
  );
};

export default ResetPassword;
