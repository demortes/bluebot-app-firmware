/*
 * main.c
 *
 *  Created on: Feb 17, 2014
 *      Author: Kevin Dethlefs-Moreno (CEEN @ UNL (Omaha Campus))
 */

#include "capi324v221.h"

//#define REPEATER
#define DEBUG

uint8_t dataSet[6];
uint8_t dataSet_defaults[6] = { 0xCA, 0x00, 0x00, 0x00, 0x00, 0x00 };
uint8_t tmpData[6];

void CBOT_main() {
	int i = 0;
	LCD_open();
	STEPPER_open();
	SPKR_open(SPKR_BEEP_MODE);
	UART_open(UART_UART0);
	UART_open(UART_UART1);
	UART_configure(UART_UART1, UART_8DBITS, UART_1SBIT, UART_NO_PARITY, 57600);
	UART_configure(UART_UART0, UART_8DBITS, UART_1SBIT, UART_NO_PARITY, 57600);
	UART_set_RX_state(UART_UART1, UART_ENABLE);
	UART_set_TX_state(UART_UART1, UART_ENABLE);
	UART_set_RX_state(UART_UART0, UART_ENABLE);
	UART_set_TX_state(UART_UART0, UART_ENABLE);

	STEPPER_set_accel(STEPPER_BOTH, 300);
#if defined(DEBUG)
	UART_printf(UART_UART0, "Debug mode enabled.\r\n");
#endif
	while (1) {
		BATTERY_check();
#if defined(REPEATER)
		if (UART0_has_data()) {
			uint8_t tmp;
			do {
				UART0_receive(&tmp);
				UART1_transmit(tmp);
			}while (UART0_has_data());
		}

		while (UART1_has_data()) {
			uint8_t tmp;
			UART1_receive(&tmp);
			UART0_transmit(tmp);
		}
#else
		if (UART1_has_data()) {
#if defined(DEBUG)
			UART_printf(UART_UART0, "Found new data.\r\n");
#endif
			for (i = 0; i < 6; i++)	// Get all six bytes, store for now
					{
				//Validate data as it comes in.
				if (i == 1 && tmpData[0] != 0xCA) {
					i = 0;
				}

				if (UART1_receive(&tmpData[i]) != UART_COMM_OK)
					UART_printf(UART_UART0, "COMM ERROR: UART1\r\n");
#if defined(DEBUG)
				UART_printf(UART_UART0, "%d: 0x%02x\r\n", i, tmpData[i]);
#endif
				UART1_transmit(0xAC);		// Send Ack
				DELAY_us(10);
			}
		}

		// If all 6 bytes received, set as data for processing.
		if (i == 6)
			if (tmpData[0] == 0xCA)
				for (i = 1; i < 6; i++)
					dataSet[i] = tmpData[i];

		//--------------------Start Processing Data --------------------

		if (dataSet[5] == 0x00)			// Left Joystick left and right
			;
		else
			;

		if (dataSet[4] == 0x00) {		// Left Joystick Up and down
			// Start Left Joystick Up/Down Processing
			STEPPER_stop(STEPPER_LEFT, STEPPER_BRK_OFF);
		} else if ((signed char) dataSet[4] > 0) {
			STEPPER_run(STEPPER_LEFT, STEPPER_FWD, (200*dataSet[4])/127);
		} else if ((signed char) dataSet[4] < 0) {
			STEPPER_run(STEPPER_LEFT, STEPPER_REV, (-200* (signed char) dataSet[4])/127);
		} 		// END Left Joystick Up / Down processing


		if (dataSet[3] == 0x00)			// Right Joystick Left & Right
			;
		else
			;

		//------------------------ Start Right Joystick U/D ------------
		if (dataSet[2] == 0x00)
			STEPPER_stop(STEPPER_RIGHT, STEPPER_BRK_OFF);
		else if ((signed char) dataSet[2] > 0)
			STEPPER_run(STEPPER_RIGHT, STEPPER_FWD, (200*dataSet[2])/127);
		else
			STEPPER_run(STEPPER_RIGHT, STEPPER_REV, (-200* (signed char) dataSet[2])/127);
		//------------------------- END RIGHT JOYSTICK U/D --------------

		//------------------------- START BUTTON PROCESSING -------------
		// First Button
		if (GBV(dataSet[1], 0))
			SPKR_beep(400);
		else
			SPKR_beep(0);

		// Second Button
		if (GBV(dataSet[1], 1))
			;
		else
			;

		// Third Button
		if (GBV(dataSet[1], 2))
			;
		else
			;

		//Fourth Button
		if (GBV(dataSet[1], 3))
			;
		else
			;

		//Fifth Button
		if (GBV(dataSet[1], 4))
			;
		else
			;

		//Sixth Button
		if (GBV(dataSet[1], 5))
			;
		else
			;

		//Seventh Button
		if (GBV(dataSet[1], 6))
			;
		else
			;

		//Eighth Button
		if (GBV(dataSet[1], 7))
			;
		else
			;

		//-------------------------- END BUTTON PROCESSING ---------------

		//-------------------------- PRINT STATUS ------------------------
		LCD_printf("- Bytes -\n");
		LCD_printf("0. 0x%02x    1. 0x%02x\n", dataSet[0], dataSet[1]);
		LCD_printf("2. 0x%02x    3. 0x%02x\n", dataSet[2], dataSet[3]);
		LCD_printf("4. 0x%02x\n", dataSet[4]);
		//-------------------------- END PRINTING ------------------------
#endif
	}
}
