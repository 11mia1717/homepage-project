import { Routes, Route } from 'react-router-dom';
import IdentityVerification from './pages/IdentityVerification';
import OtpInput from './pages/OtpInput';

function App() {
  return (
    <div className="min-h-screen bg-[#F5F6F8] flex justify-center">
      <div className="w-full max-w-[480px] min-h-screen bg-white shadow-xl shadow-gray-200/50">
        <Routes>
          <Route path="/" element={<IdentityVerification />} />
          <Route path="/verify" element={<IdentityVerification />} />
          <Route path="/verify/otp" element={<OtpInput />} />
        </Routes>
      </div>
    </div>
  );
}

export default App;
