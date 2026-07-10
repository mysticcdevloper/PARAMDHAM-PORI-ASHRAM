package com.example.database

import kotlinx.coroutines.flow.Flow

class SadhanaRepository(private val sadhanaLogDao: SadhanaLogDao) {
  val allLogs: Flow<List<SadhanaLog>> = sadhanaLogDao.getAllLogs()

  suspend fun getLogByDate(date: String): SadhanaLog? {
    return sadhanaLogDao.getLogByDate(date)
  }

  suspend fun insertLog(log: SadhanaLog) {
    sadhanaLogDao.insertLog(log)
  }

  suspend fun deleteLogById(id: Int) {
    sadhanaLogDao.deleteLogById(id)
  }

  suspend fun clearLogs() {
    sadhanaLogDao.clearAllLogs()
  }
}
