package com.example.core.config

import android.util.Log
import com.example.BuildConfig

object SupabaseConfig {
  val supabaseUrl: String = try {
    BuildConfig.SUPABASE_URL
  } catch (e: Exception) {
    "https://your-supabase-project.supabase.co"
  }

  val supabaseAnonKey: String = try {
    BuildConfig.SUPABASE_ANON_KEY
  } catch (e: Exception) {
    "your-anon-key-placeholder"
  }

  fun isConfigured(): Boolean {
    return supabaseUrl.isNotEmpty() && 
           supabaseUrl != "https://your-supabase-project.supabase.co" &&
           supabaseAnonKey.isNotEmpty() && 
           supabaseAnonKey != "your-anon-key-placeholder"
  }

  init {
    Log.d("SupabaseConfig", "Initializing Supabase Client...")
    Log.d("SupabaseConfig", "Supabase URL: $supabaseUrl")
    Log.d("SupabaseConfig", "Supabase Anon Key is present: ${supabaseAnonKey.isNotEmpty()}")
  }
}
