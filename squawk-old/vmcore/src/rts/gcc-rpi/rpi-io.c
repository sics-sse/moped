#ifndef SHALLOW_SLEEP_CLOCK_MODE_NORMAL
#define SHALLOW_SLEEP_CLOCK_MODE_NORMAL 0
#endif

#ifndef SLEEP_MANAGER_ENABLED_IRQS
#define SLEEP_MANAGER_ENABLED_IRQS 0
#endif

#define DEVICE_DEFAULT 1
#define DEVICE_USB 2
#define DEVICE_SERIAL 3
#define DEVICE_FIRST DEVICE_DEFAULT
#define DEVICE_LAST DEVICE_SERIAL

#define WAIT_FOR_DEEP_SLEEP_EVENT_NUMBER (DEVICE_LAST+1)
#define FIRST_IRQ_EVENT_NUMBER (WAIT_FOR_DEEP_SLEEP_EVENT_NUMBER+1)

volatile long long clock_counter;

//static void ioExecute(void) {
//	printf("ioExecute in gcc-rpi\n");
//}

static void rpiPrint(char *str);
static void rpiPrintln(char *str);

static void ioExecuteSys(void) {
	int res = ChannelConstants_RESULT_OK;

	//  int     context = com_sun_squawk_ServiceOperation_context;
	int op = com_sun_squawk_ServiceOperation_op;
	//  int     channel = com_sun_squawk_ServiceOperation_channel;
	int i1 = com_sun_squawk_ServiceOperation_i1;
	int i2 = com_sun_squawk_ServiceOperation_i2;
	int i3 = com_sun_squawk_ServiceOperation_i3;
	int i4 = com_sun_squawk_ServiceOperation_i4;
	int i5 = com_sun_squawk_ServiceOperation_i5;
	int i6 = com_sun_squawk_ServiceOperation_i6;
	Address o1 = com_sun_squawk_ServiceOperation_o1;
	Address receive = com_sun_squawk_ServiceOperation_o2;

	switch (op) {
		case ChannelConstants_RPI_PRINTSTRING: {
			rpiPrint(o1);
			break;
		}
		default: {
			rpiPrintln("Default print operation (something's wrong)");
		}
	}

	com_sun_squawk_ServiceOperation_result = res;
}

static void rpiPrint(char *str) {
#ifdef AUTOSAR
  int length = getArrayLength(str);
  output2(str, length);
#else
	printf("%s", str);
#endif
}
static void rpiPrintln(char *str) {
	rpiPrint(str);
	rpiPrint("\r\n");
}
