
import axios from "axios";
import {
  REGISTER_REQUEST,
  REGISTER_SUCCESS,
  REGISTER_FAILURE,
  LOGIN_REQUEST,
  LOGIN_SUCCESS,
  LOGIN_FAILURE,
  GET_USER_REQUEST,
  GET_USER_SUCCESS,
  GET_USER_FAILURE,
  LOGOUT,
} from "./ActionTypes";

import { API_BASE_URL } from "@/Api/api";

// ==================== REGISTER ====================

const registerRequest = () => ({
  type: REGISTER_REQUEST,
});

const registerSuccess = (user) => ({
  type: REGISTER_SUCCESS,
  payload: user,
});

const registerFailure = (error) => ({
  type: REGISTER_FAILURE,
  payload: error,
});

export const register = (userData) => async (dispatch) => {
  dispatch(registerRequest());

  try {
    const response = await axios.post(
      `${API_BASE_URL}/auth/signup`,
      userData
    );

    const user = response.data;

    if (user.jwt) {
      localStorage.setItem("jwt", user.jwt);
    }

    console.log("Register success:", user);

    dispatch(registerSuccess(user));

  } catch (error) {
    console.log("Register error:", error);

    dispatch(
      registerFailure(
        error.response?.data?.message || error.message
      )
    );
  }
};


// ==================== LOGIN ====================

const loginRequest = () => ({
  type: LOGIN_REQUEST,
});

const loginSuccess = (user) => ({
  type: LOGIN_SUCCESS,
  payload: user,
});

const loginFailure = (error) => ({
  type: LOGIN_FAILURE,
  payload: error,
});

export const login = (userData) => async (dispatch) => {
  dispatch(loginRequest());

  try {
    const response = await axios.post(
      `${API_BASE_URL}/auth/signin`,
      userData
    );

    const user = response.data;

    if (user.jwt) {
      localStorage.setItem("jwt", user.jwt);
    }

    console.log("Login success:", user);

    dispatch(loginSuccess(user));

  } catch (error) {
    console.log("Login error:", error);

    dispatch(
      loginFailure(
        error.response?.data?.message || error.message
      )
    );
  }
};


// ==================== GET USER PROFILE ====================

export const getUser = (token) => {
  return async (dispatch) => {
    dispatch({
      type: GET_USER_REQUEST,
    });

    try {
      const response = await axios.get(
        `${API_BASE_URL}/api/users/profile`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      const user = response.data;

      dispatch({
        type: GET_USER_SUCCESS,
        payload: user,
      });

      console.log("User profile:", user);

    } catch (error) {
      console.log("Get user error:", error);

      dispatch({
        type: GET_USER_FAILURE,
        payload:
          error.response?.data?.message || error.message,
      });
    }
  };
};


// ==================== LOGOUT ====================

export const logout = () => {
  return async (dispatch) => {
    localStorage.removeItem("jwt");

    dispatch({
      type: LOGOUT,
    });
  };
};
