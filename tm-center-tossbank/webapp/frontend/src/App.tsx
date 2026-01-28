import { Routes, Route, Navigate } from 'react-router-dom'
import Layout from './components/layout/Layout'
import AgentLayout from './components/layout/AgentLayout'
import AdminLayout from './components/layout/AdminLayout'
import HomePage from './pages/HomePage'
import ConsentPage from './pages/ConsentPage'
import MyConsentPage from './pages/MyConsentPage'
import AgentDashboard from './pages/agent/AgentDashboard'
import AgentCallPage from './pages/agent/AgentCallPage'
import AdminDashboard from './pages/admin/AdminDashboard'
import AdminAccessLogs from './pages/admin/AdminAccessLogs'
import AdminDestruction from './pages/admin/AdminDestruction'
import LoginPage from './pages/LoginPage'

function App() {
  return (
    <Routes>
      {/* 공개 페이지 */}
      <Route path="/" element={<Layout />}>
        <Route index element={<HomePage />} />
        <Route path="consent/:productName" element={<ConsentPage />} />
        <Route path="my-consent" element={<MyConsentPage />} />
        <Route path="login" element={<LoginPage />} />
      </Route>

      {/* 상담사 페이지 */}
      <Route path="/agent" element={<AgentLayout />}>
        <Route index element={<AgentDashboard />} />
        <Route path="call" element={<AgentCallPage />} />
        <Route path="call/:targetId" element={<AgentCallPage />} />
      </Route>

      {/* 관리자 페이지 */}
      <Route path="/admin" element={<AdminLayout />}>
        <Route index element={<AdminDashboard />} />
        <Route path="access-logs" element={<AdminAccessLogs />} />
        <Route path="destruction" element={<AdminDestruction />} />
      </Route>

      {/* 404 */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default App
