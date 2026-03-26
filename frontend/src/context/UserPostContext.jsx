import React, { createContext, useContext, useState } from 'react';

const UserPostContext = createContext(null);

export const useUserPost = () => useContext(UserPostContext);

export const UserPostProvider = ({ children }) => {
  const [userId, setUserId] = useState(null);

  return (
    <UserPostContext.Provider value={{ userId, setUserId }}>
      {children}
    </UserPostContext.Provider>
  );
};