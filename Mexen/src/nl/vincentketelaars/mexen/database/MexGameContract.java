package nl.vincentketelaars.mexen.database;

import android.provider.BaseColumns;

public class MexGameContract {

	private static final String TEXT_TYPE = " TEXT";
	private static final String INT_TYPE = " INTEGER";
	private static final String PRIMARY = " PRIMARY KEY";
	private static final String COMMA_SEP = ", ";
	

	public MexGameContract() {}

    /* Inner class that defines the table contents */
    public static abstract class GameEntry implements BaseColumns {
        public static final String TABLE_NAME = "games";
        public static final String COLUMN_NAME_GAME_ID = "gid";
        public static final String COLUMN_NAME_GAME_MODE = "gamemode";
        public static final String COLUMN_NAME_START_TIME = "start";
        public static final String COLUMN_NAME_FINISH_TIME = "finish";
        
        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                COLUMN_NAME_GAME_ID + TEXT_TYPE + PRIMARY + COMMA_SEP +
                COLUMN_NAME_GAME_MODE + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_START_TIME + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_FINISH_TIME + INT_TYPE + " )";
        
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
    
    /* Inner class that defines the table contents */
    public static abstract class PlayerEntry implements BaseColumns {
        public static final String TABLE_NAME = "players";
        public static final String COLUMN_NAME_GAME_ID = "gid";
        public static final String COLUMN_NAME_PLAYER_ID = "pid";
        public static final String COLUMN_NAME_PLAYER_NAME = "name";
        public static final String COLUMN_NAME_PLAYER_LOCAL = "local";
        public static final String COLUMN_NAME_PLAYER_CREATION_DATE_TIME = "datetime";
        
        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                COLUMN_NAME_GAME_ID + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_PLAYER_ID + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_PLAYER_NAME + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_PLAYER_LOCAL + INT_TYPE + COMMA_SEP + // Boolean 0 or 1
                COLUMN_NAME_PLAYER_CREATION_DATE_TIME + INT_TYPE + COMMA_SEP +              
                "UNIQUE(" + COLUMN_NAME_GAME_ID + COMMA_SEP + COLUMN_NAME_PLAYER_ID + "))"; // Long epoch millis
        
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
    
    /* Inner class that defines the table contents */
    public static abstract class TurnEntry implements BaseColumns {
        public static final String TABLE_NAME = "turns";
        public static final String COLUMN_NAME_GAME_ID = "gid";
        public static final String COLUMN_NAME_TURN_ID = "tid";
        public static final String COLUMN_NAME_PLAYER_ID = "pid";
        public static final String COLUMN_NAME_FINISH_TIME = "finish";
        public static final String COLUMN_NAME_THROW_NUMBER = "throw_num";
        
        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                COLUMN_NAME_TURN_ID + TEXT_TYPE + PRIMARY + COMMA_SEP +
                COLUMN_NAME_GAME_ID + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_FINISH_TIME + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_THROW_NUMBER + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_PLAYER_ID + TEXT_TYPE + " )";
        
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
    
    /* Inner class that defines the table contents */
    public static abstract class ThrowEntry implements BaseColumns {
        public static final String TABLE_NAME = "throws";
        public static final String COLUMN_NAME_THROW_ID = "trid";
        public static final String COLUMN_NAME_TURN_ID = "tid";
        public static final String COLUMN_NAME_THROW_DATE_TIME = "datetime";
        public static final String COLUMN_NAME_THROW_ONE = "t1";
        public static final String COLUMN_NAME_THROW_TWO = "t2";
        public static final String COLUMN_NAME_THROW_THREE = "t3"; // Can be zero
        public static final String COLUMN_NAME_VAST_ONE = "v1";
        public static final String COLUMN_NAME_VAST_TWO = "v2";
        public static final String COLUMN_NAME_VAST_THREE = "v3"; // Can be zero
        
        // We assume here that we're more likely to search for the final throw, than for an entire turn
        // Hence we make the turn id primary
        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                COLUMN_NAME_THROW_ID + TEXT_TYPE + PRIMARY + COMMA_SEP +
                COLUMN_NAME_TURN_ID + TEXT_TYPE + COMMA_SEP +
                COLUMN_NAME_THROW_DATE_TIME + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_VAST_ONE + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_VAST_TWO + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_VAST_THREE + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_THROW_ONE + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_THROW_TWO + INT_TYPE + COMMA_SEP +
                COLUMN_NAME_THROW_THREE + INT_TYPE + " )";
        
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}