package com.example.calculadora;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface HistoryDao {
    @Insert
    // Entry é um objeto do tipo HistoryEntry
    void insert(HistoryEntry entry);

    @Query("SELECT * FROM history ORDER BY id ASC")
    List<HistoryEntry> getAllHistory();

    @Query("DELETE FROM history")
    void clearHistory();
}
