import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { User } from '../types';

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  login: (user: User) => void;
  logout: () => void;
  setAgentId: (id: number) => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      isAuthenticated: false,
      
      login: (user) => {
        localStorage.setItem('agentId', String(user.id));
        set({ user, isAuthenticated: true });
      },
      
      logout: () => {
        localStorage.removeItem('agentId');
        set({ user: null, isAuthenticated: false });
      },

      setAgentId: (id) => {
        localStorage.setItem('agentId', String(id));
      },
    }),
    {
      name: 'auth-storage',
    }
  )
);
