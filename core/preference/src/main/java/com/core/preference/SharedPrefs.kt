package com.core.preference

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Lớp quản lý SharedPreferences đã được tối ưu hóa.
 * - Cho phép tạo instance trực tiếp hoặc sử dụng Singleton.
 * - Dùng inline function và reified type parameter cho các hàm get/put.
 * - Hỗ trợ ủy quyền thuộc tính (Property Delegation) qua hàm `preference(...)`.
 * - Sửa lỗi lưu trữ và truy xuất kiểu Double.
 * - Sử dụng @PublishedApi để cho phép inline function truy cập thành viên non-public.
 */
class SharedPrefs constructor(context: Context, name: String?, @PublishedApi internal val gson: Gson) {

    @PublishedApi
    internal val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(name ?: context.packageName, Context.MODE_PRIVATE)

    /**
     * Tạo một delegate cho một thuộc tính SharedPreferences.
     * Cho phép sử dụng cú pháp `by prefs.preference(...)`.
     *
     * @param T Kiểu dữ liệu của preference.
     * @param defaultValue Giá trị mặc định nếu không tìm thấy key.
     * @param key Khóa của preference. Nếu là null, tên của thuộc tính sẽ được sử dụng làm khóa.
     * @return Một đối tượng ReadWriteProperty để ủy quyền.
     *
     * @example
     * var isPaidUser by prefs.preference(false)
     * var username by prefs.preference("", key = "user_name")
     */
    inline fun <reified T> preference(defaultValue: T, key: String? = null, crossinline onAfterSet: ((T) -> Unit)): ReadWriteProperty<Any, T> {
        return object : ReadWriteProperty<Any, T> {
            override fun getValue(thisRef: Any, property: KProperty<*>): T {
                return get(key ?: property.name, defaultValue)
            }

            override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
                put(key ?: property.name, value)
                onAfterSet.invoke(value)
            }
        }
    }

    inline fun <reified T> preference(defaultValue: T, key: String? = null): ReadWriteProperty<Any, T> {
        return object : ReadWriteProperty<Any, T> {
            override fun getValue(thisRef: Any, property: KProperty<*>): T {
                return get(key ?: property.name, defaultValue)
            }

            override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
                put(key ?: property.name, value)
            }
        }
    }

    /**
     * Tạo một delegate cho một thuộc tính SharedPreferences.
     * Cho phép sử dụng cú pháp `by prefs.preference(...)`.
     *
     * @param T Kiểu dữ liệu của preference.
     * @param defaultValue Giá trị mặc định nếu không tìm thấy key.
     * @param key Khóa của preference. Nếu là null, tên của thuộc tính sẽ được sử dụng làm khóa.
     * @return Một đối tượng ReadWriteProperty để ủy quyền.
     *
     * @example
     * var isPaidUser by prefs.preference(false)
     * var username by prefs.preference("", key = "user_name")
     */
    inline fun <reified T> preferenceNullable(defaultValue: T?, key: String? = null): ReadWriteProperty<Any, T?> {
        return object : ReadWriteProperty<Any, T?> {
            override fun getValue(thisRef: Any, property: KProperty<*>): T? {
                return if(defaultValue == null) get(key ?: property.name) else get(key ?: property.name, defaultValue)
            }

            override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
                put(key ?: property.name, value)
            }
        }
    }

    /**
     * Lưu một giá trị vào SharedPreferences.
     * @param key Khóa để lưu.
     * @param data Dữ liệu cần lưu. Kiểu dữ liệu sẽ được tự động nhận diện.
     * Nếu data là null, cặp key-value tương ứng sẽ bị xóa.
     */
    inline fun <reified T> put(key: String, data: T?) {
        sharedPreferences.edit {
            when (data) {
                is String -> putString(key, data)
                is Boolean -> putBoolean(key, data)
                is Float -> putFloat(key, data)
                is Int -> putInt(key, data)
                is Long -> putLong(key, data)
                // SharedPreferences không hỗ trợ Double, ta lưu nó dưới dạng String
                is Double -> putString(key, data.toString())
                null -> remove(key)
                // Các kiểu dữ liệu khác sẽ được chuyển thành chuỗi JSON
                else -> putString(key, gson.toJson(data))
            }
        }
    }

    /**
     * Lấy một giá trị từ SharedPreferences.
     * @param key Khóa của giá trị cần lấy.
     * @return Trả về giá trị kiểu T, hoặc null nếu không tìm thấy.
     */
    inline fun <reified T> get(key: String): T? {
        // Gán kết quả của when cho một biến để tránh cảnh báo ép kiểu ngầm định
        val result: Any? = when (T::class) {
            String::class -> sharedPreferences.getString(key, null)
            Boolean::class -> sharedPreferences.getBoolean(key, false).takeIf { sharedPreferences.contains(key) }
            Float::class -> sharedPreferences.getFloat(key, 0f).takeIf { sharedPreferences.contains(key) }
            Int::class -> sharedPreferences.getInt(key, 0).takeIf { sharedPreferences.contains(key) }
            Long::class -> sharedPreferences.getLong(key, 0L).takeIf { sharedPreferences.contains(key) }
            // Lấy giá trị Double đã được lưu dưới dạng String
            Double::class -> sharedPreferences.getString(key, null)?.toDoubleOrNull()
            else -> {
                val json = sharedPreferences.getString(key, null)
                json?.let { gson.fromJson(it, T::class.java) }
            }
        }
        return result as T?
    }

    /**
     * Lấy một giá trị từ SharedPreferences với giá trị mặc định.
     * @param key Khóa của giá trị cần lấy.
     * @param defaultValue Giá trị mặc định sẽ được trả về nếu không tìm thấy key.
     * @return Trả về giá trị kiểu T, hoặc defaultValue nếu không tìm thấy.
     */
    inline fun <reified T> get(key: String, defaultValue: T): T {
        val result = when (T::class) {
            String::class -> sharedPreferences.getString(key, defaultValue as String)
            Boolean::class -> sharedPreferences.getBoolean(key, defaultValue as Boolean)
            Float::class -> sharedPreferences.getFloat(key, defaultValue as Float)
            Int::class -> sharedPreferences.getInt(key, defaultValue as Int)
            Long::class -> sharedPreferences.getLong(key, defaultValue as Long)
            Double::class -> {
                val stringValue = sharedPreferences.getString(key, null)
                stringValue?.toDoubleOrNull() ?: (defaultValue as Double)
            }
            else -> {
                val json = sharedPreferences.getString(key, null)
                json?.let { gson.fromJson(it, T::class.java) } ?: defaultValue
            }
        }
        // Ép kiểu ở cuối là an toàn vì logic `when` đã đảm bảo đúng kiểu
        @Suppress("UNCHECKED_CAST")
        return result as T
    }

    /**
     * Xóa toàn bộ dữ liệu trong SharedPreferences file này.
     */
    fun clear() {
        sharedPreferences.edit { clear() }
    }

    /**
     * Xóa một giá trị khỏi SharedPreferences theo key.
     * @param key Khóa của giá trị cần xóa.
     */
    fun remove(key: String) {
        sharedPreferences.edit { remove(key) }
    }


    companion object {
        // @Volatile đảm bảo rằng giá trị của INSTANCE luôn được cập nhật trên mọi luồng
        @Volatile
        private var INSTANCE: SharedPrefs? = null

        /**
         * Khởi tạo Singleton instance. Phải được gọi một lần trong Application.onCreate().
         * @param context Context của ứng dụng.
         * @param name Tên của SharedPreferences file. Mặc định là package name.
         * @param gson Một instance của Gson. Cho phép tùy chỉnh nếu cần.
         */
        fun init(context: Context, name: String? = null, gson: Gson = Gson()) {
            // Sử dụng synchronized để đảm bảo an toàn trong môi trường đa luồng
            synchronized(this) {
                if (INSTANCE == null) {
                    // Sử dụng applicationContext để tránh memory leak
                    INSTANCE = SharedPrefs(context.applicationContext, name, gson)
                }
            }
        }

        /**
         * Lấy Singleton instance.
         * Sẽ throw IllegalStateException nếu chưa được khởi tạo.
         */
        val instance: SharedPrefs
            get() = INSTANCE ?: throw IllegalStateException(
                "SharedPrefs not initialized. Please call SharedPrefs.init(context) in your Application's onCreate() method."
            )
    }
}
