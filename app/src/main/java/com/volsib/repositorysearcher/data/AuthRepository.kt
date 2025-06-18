package com.volsib.repositorysearcher.data

import android.content.SharedPreferences
import com.volsib.repositorysearcher.models.LoginRequest
import com.volsib.repositorysearcher.models.RegisterRequest
import com.volsib.repositorysearcher.models.User

class AuthRepository(
    private val userDao: UserDao,
    private val sharedPreferences: SharedPreferences
) {
    companion object {
        const val PREF_CURRENT_USER_ID = "current_user_id"
        const val PREF_IS_LOGGED_IN = "is_logged_in"
    }
    
    // Регистрация
    suspend fun register(request: RegisterRequest): Result<User> {
        return try {
            // Валидация данных
            if (request.username.isBlank() || request.email.isBlank() || request.password.isBlank()) {
                return Result.failure(Exception("Все поля должны быть заполнены"))
            }
            
            if (request.password != request.confirmPassword) {
                return Result.failure(Exception("Пароли не совпадают"))
            }
            
            if (request.password.length < 6) {
                return Result.failure(Exception("Пароль должен содержать минимум 6 символов"))
            }
            
            // Проверка существования пользователя
            val existingUser = userDao.getUserByUsername(request.username)
            if (existingUser != null) {
                return Result.failure(Exception("Пользователь с таким именем уже существует"))
            }
            
            val existingEmail = userDao.getUserByEmail(request.email)
            if (existingEmail != null) {
                return Result.failure(Exception("Пользователь с такой почтой уже существует"))
            }
            
            // Создание нового пользователя
            val newUser = User(
                username = request.username,
                email = request.email,
                password = request.password
            )
            
            val userId = userDao.insertUser(newUser)
            val createdUser = newUser.copy(id = userId.toInt())
            
            Result.success(createdUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Авторизация
    suspend fun login(request: LoginRequest): Result<User> {
        return try {
            // Поиск пользователя
            val user = userDao.getUserByUsername(request.username)
            if (user == null) {
                return Result.failure(Exception("Пользователь не найден"))
            }
            
            // Проверка пароля
            if (user.password != request.password) {
                return Result.failure(Exception("Неверный пароль"))
            }
            
            // Обновление времени последнего входа
            userDao.updateLastLogin(user.id, System.currentTimeMillis())
            
            // Сохранение состояния авторизации
            sharedPreferences.edit()
                .putInt(PREF_CURRENT_USER_ID, user.id)
                .putBoolean(PREF_IS_LOGGED_IN, true)
                .apply()
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Выход
    fun logout() {
        sharedPreferences.edit()
            .remove(PREF_CURRENT_USER_ID)
            .putBoolean(PREF_IS_LOGGED_IN, false)
            .apply()
    }
    
    // Проверка авторизации
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(PREF_IS_LOGGED_IN, false)
    }
    
    // Получение текущего пользователя
    suspend fun getCurrentUser(): User? {
        val userId = sharedPreferences.getInt(PREF_CURRENT_USER_ID, -1)
        return if (userId != -1) userDao.getUserById(userId) else null
    }
} 