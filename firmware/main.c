/*
 * Copyright (C) 2014 The Board of Regents of the University of Nebraska.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

#include "capi324v221.h"
#include <string.h>

//#define REPEATER
#define DEBUG

void speed_change(int change);
void setup_hc();

uint8_t dataSet[6];
uint8_t dataSet_defaults[6] = { 0xCA, 0x00, 0x00, 0x00, 0x00, 0x00 };
uint8_t tmpData[6];
int speed = 200;

void CBOT_main() {

	LCD_open();
	STEPPER_open();
	DDRA |= (1 << PA3);
	SPKR_open(SPKR_BEEP_MODE);
	UART_open(UART_UART0);
	UART_open(UART_UART1);
	UART_configure(UART_UART1, UART_8DBITS, UART_1SBIT, UART_NO_PARITY, 57600);
	UART_configure(UART_UART0, UART_8DBITS, UART_1SBIT, UART_NO_PARITY, 57600);
	UART_set_RX_state(UART_UART1, UART_ENABLE);
	UART_set_TX_state(UART_UART1, UART_ENABLE);
	UART_set_RX_state(UART_UART0, UART_ENABLE);
	UART_set_TX_state(UART_UART0, UART_ENABLE);
//	PORTA |= (1 << PA3);

	//Test Bluetooth module, if it responds at 57600 continue. If not, call setup_hc()
	DELAY_ms(3000);
	UART_printf(UART_UART1, "AT\r\n");
	DELAY_us(100);
#ifndef REPEATER
	int i = 0;
//	if(!UART1_has_data())
//		setup_hc();
//	else
#endif
//	{
//		char response[4];
//		int i = 0;
//		while(UART1_has_data())
//		{
//			UART1_receive((unsigned char *) &response[i++]);
//			if(i >= 4)
//			{
//				UART_printf(UART_UART0, "UART1 has more data than it should!");
//				break;
//			}
//		}
//		if(strncmp(response, "OK\r\n", 4))
//		{
//			setup_hc();
//		}
//	}

	STEPPER_set_accel(STEPPER_BOTH, 200);
#if defined(DEBUG)
	UART_printf(UART_UART0, "Debug mode enabled.\r\n");
#endif
#if defined(REPEATER)
	UART_printf(UART_UART0, "REPEAT mode enabled.");
#endif
	while (1) {
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

//		if (dataSet[5] == 0x00)			// Left Joystick left and right
//			;
//		else
//			;

		if (dataSet[4] == 0x00) {		// Left Joystick Up and down
			// Start Left Joystick Up/Down Processing
			STEPPER_stop(STEPPER_LEFT, STEPPER_BRK_OFF);
		} else if ((signed char) dataSet[4] > 0) {
			STEPPER_run(STEPPER_LEFT, STEPPER_FWD, (speed * dataSet[4]) / 127);
		} else if ((signed char) dataSet[4] < 0) {
			STEPPER_run(STEPPER_LEFT, STEPPER_REV,
					(-speed * (signed char) dataSet[4]) / 127);
		} 		// END Left Joystick Up / Down processing

//		if (dataSet[3] == 0x00)			// Right Joystick Left & Right
//			;
//		else
//			;

		//------------------------ Start Right Joystick U/D ------------
		if (dataSet[2] == 0x00)
			STEPPER_stop(STEPPER_RIGHT, STEPPER_BRK_OFF);
		else if ((signed char) dataSet[2] > 0) {
			STEPPER_run(STEPPER_RIGHT, STEPPER_FWD, (speed * dataSet[2]) / 127);
		} else if ((signed char) dataSet[2] < 0) {
			STEPPER_run(STEPPER_RIGHT, STEPPER_REV,
					(-speed * (signed char) dataSet[2]) / 127);
		}
		//------------------------- END RIGHT JOYSTICK U/D --------------

		//------------------------- START BUTTON PROCESSING -------------
		// L2 button
		if (dataSet[1] & 0x01)
			speed_change(-1);

		// R2 Button
		if (dataSet[1] & 0x02)
			speed_change(1);

		// L1 Button
//		if (GBV(dataSet[1], 2))
//			;

		//R1 Button
//		if (GBV(dataSet[1], 3))
//			;

		//Triangle Button
//		if (GBV(dataSet[1], 4))
//			;

		//Circle Button
//		if (GBV(dataSet[1], 5))
//			;

		//X Button
		if (dataSet[1] & 0x40)
			SPKR_beep(400);
		else
			SPKR_beep(0);

		//Square Button
//		if (GBV(dataSet[1], 7))
//			;

		//-------------------------- END BUTTON PROCESSING ---------------

		//-------------------------- PRINT STATUS ------------------------
		LCD_printf("- Bytes -\n");
		LCD_printf("0. 0x%02x    1. 0x%02x\n", dataSet[0], dataSet[1]);
		LCD_printf("2. 0x%02x    3. 0x%02x\n", dataSet[2], dataSet[3]);
		LCD_printf("4. 0x%02x    S. %03d\n", dataSet[4], speed);
		//-------------------------- END PRINTING ------------------------
#endif
	}
}

void speed_change(int change) {
#ifdef DEBUG
	UART_printf(UART_UART0, "SPEED: Byte: 0x%02x\n", dataSet[1]);
#endif
	if ((speed < 300)) {
		if (change > 0) {
			speed++;
		}
	}
	if (speed > 0) {
		if (change < 0) {
			speed--;
		}
	}
}

void setup_hc()
{
#ifdef DEBUG
	UART_printf(UART_UART0, "Detected illegible baud rate.\r\n");
#endif
	SBV(3, PORTA);			// Throw HC-05 into AT mode
	UART_configure(UART_UART1, UART_8DBITS, UART_1SBIT, UART_NO_PARITY, 9600);
	UART_set_RX_state(UART_UART1, UART_ENABLE);
	UART_set_TX_state(UART_UART1, UART_ENABLE);
#ifdef DEBUG
	UART_printf(UART_UART0, "Changed speed.\r\n");
#endif
	UART_printf(UART_UART1, "AT\r\n");
	DELAY_ms(30);
	UART_printf(UART_UART1, "AT+UART=57600,0,0\r\n");
	UART_printf(UART_UART1, "AT+BAUD7\r\n");
#ifdef DEBUG
	UART_printf(UART_UART0, "Sent AT+UART command.\r\n");
#endif
	DELAY_ms(30);
	CBV(3, PORTA);

	UART_configure(UART_UART1, UART_8DBITS, UART_1SBIT, UART_NO_PARITY, 57600);
	UART_set_RX_state(UART_UART1, UART_ENABLE);
	UART_set_TX_state(UART_UART1, UART_ENABLE);
#ifdef DEBUG
	UART_printf(UART_UART0, "Configured HC.\r\n");
#endif
}
