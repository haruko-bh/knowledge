SELECT * FROM USER_CONFIGS
 WHERE 
CONFIG_NAME = ?
 AND SYSTEM_NAME = ?
 AND USER_ID = ?
 AND DELETE_FLAG = 0;
