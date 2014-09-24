/*
 * BSD LICENSE
 * Copyright (c) 2012, Mobile Unit of G+J Electronic Media Sales GmbH, Hamburg All rights reserved.
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer .
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The source code is just allowed for private use, not for commercial use.
 *
 */
#import "GUJNativeEmailComposer.h"

@implementation GUJNativeEmailComposer

@synthesize mailComposeVC = _mailComposeVC;

#pragma mark MFMailComposeViewController delegate
- (void)mailComposeController:(MFMailComposeViewController *)controller didFinishWithResult:(MFMailComposeResult)result error:(NSError *)error
{
#pragma unused(error,result)
    [controller dismissModalViewControllerAnimated:YES];
}

#pragma mark public methods
- (id)init
{
    self = [super init];
    if( self ) {
        [super __setRequiredDeviceCapability:GUJDeviceCapabilityEmail];
    }
    return self;
}

- (BOOL)canComposeEmail
{
    return ([GUJUtil iosVersion] >= __IPHONE_3_0 && [self isAvailableForCurrentDevice] && [MFMailComposeViewController canSendMail]);
}

- (BOOL)composeEmailTo:(NSString*)recipient subject:(NSString*)subject body:(NSString*)body
{
    BOOL result = NO;
    [NSObject cancelPreviousPerformRequestsWithTarget:self];
    if( [self canComposeEmail] ) {
        _mailComposeVC = [[MFMailComposeViewController alloc] init];
        [_mailComposeVC setMailComposeDelegate:self];
        [_mailComposeVC setToRecipients:[NSArray arrayWithObject:recipient]];
        [_mailComposeVC setSubject:subject];
        [_mailComposeVC setMessageBody:body isHTML:[self isHTMLContent]];
        [GUJUtil showPresentModalViewController:_mailComposeVC];
        result = YES;
    }
    return result;
}


@end
