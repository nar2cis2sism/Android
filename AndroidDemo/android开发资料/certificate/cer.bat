set keystore=androidEngine.keystore
set file=androidEngine.cer

keytool -export -keystore %keystore% -alias androidEngine -file %file% -rfc

:: -exportָ��Ϊ��������
:: -keystoreָ��keystore�ļ�
:: -aliasָ������keystore�ļ��еı���
:: -fileָ�򵼳�·��
:: -rfc���ı���ʽ�����Ҳ������BASE64�������

pause