 // My AIDL file, named SomeClass.aidl
 package demo.aidl;
 // See the list above for which classes need
 // import statements (hint--most of them)
 // Declare the interface.
 interface IAidl {
   // Methods can take 0 or more parameters, and
   // return a value or void.
   int getAccountBalance();
   void setOwnerNames(in List<String> names);
   // Methods can even take other AIDL-defined parameters.
   // BankAccount createAccount(in String name, int startingDeposit, in IAtmService atmService);
   // All non-Java primitive parameters (e.g., int, bool, etc) require
   // a directional tag indicating which way the data will go. Available
   // values are in, out, inout. (Primitives are in by default, and cannot be otherwise).
   // Limit the direction to what is truly needed, because marshalling parameters
   // is expensive.
   int getCustomerList(in String branch, out String[] customerList);
   
   void showTest();
 }
