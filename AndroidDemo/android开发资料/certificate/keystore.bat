set keystore=androidEngine.keystore

keytool -genkey -validity 36000 -alias androidEngine -keyalg RSA -keystore %keystore%

:: -genkey��ʾ������Կ
:: -validityָ��֤����Ч�ڣ�������36000��
:: -aliasָ��������������androidEngine
:: -keyalgָ���㷨��������RSA
:: -keystoreָ���洢λ��

pause