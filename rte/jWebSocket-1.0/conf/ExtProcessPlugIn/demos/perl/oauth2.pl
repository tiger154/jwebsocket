#	---------------------------------------------------------------------------
#	jWebSocket - Perl OAuth Client (Community Edition, CE)
#	---------------------------------------------------------------------------
#	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
#	Alexander Schulze, Germany (NRW)
#
#	Licensed under the Apache License, Version 2.0 (the "License");
#	you may not use this file except in compliance with the License.
#	You may obtain a copy of the License at
#
#	http://www.apache.org/licenses/LICENSE-2.0
#
#	Unless required by applicable law or agreed to in writing, software
#	distributed under the License is distributed on an "AS IS" BASIS,
#	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#	See the License for the specific language governing permissions and
#	limitations under the License.
#	---------------------------------------------------------------------------

# this code is based on: 
# http://tools.ietf.org/html/rfc6749 - The OAuth 2.0 Authorization Framework
# http://tools.ietf.org/html/rfc6750 - The OAuth 2.0 Authorization Framework: Bearer Token Usage
# http://oauth.net/2/ - OAuth 2.0

use Data::Dumper;
use LWP::UserAgent;
use HTTP::Request::Common;
use JSON;

# test settings
my $access_token = "";
my $refresh_token = "";
my $client_secret = 'xxxx';
my $base_url = 'https://server.tld/as/token.oauth2';
my $username = 'aschulze@jwebsocket.org';
my $password = 'xxxx';

# initiate the user agent (the http(s) web client) 
my $ua = LWP::UserAgent->new();
$ua->timeout(10);
$ua->env_proxy;

# we need to turn off verification, since we have not CA's stored in Perl.
$ua->ssl_opts( SSL_verify_mode => SSL_VERIFY_NONE );

sub callOAuth2API {
	my $response = $ua->request( POST $base_url, [ @_ ] );

	#if ( $response->is_success ) {
	#	printf( "Response: %s, %s, %s\n", $response->status_line, $response->code, $response->message );
	#	printf( "Content: %s\n", $response->decoded_content );
	#} else {
	#	die $response->status_line;
	#}

	# generate a Perl object from the received JSON string
	$lJSON = from_json( $response->decoded_content );
	return( $lJSON );
}

sub ssoAuthDirect {
	my( $aUsername, $aPassword ) = @_;

	my $lJSON= callOAuth2API([
		client_id => 'ro_client',
		grant_type => 'password',
		username => $aUsername,
		password => $aPassword
	]);

	if( defined $lJSON->{'access_token'}
		&& defined $lJSON->{'refresh_token'} ) {
		$access_token = $lJSON->{'access_token'};
		$refresh_token = $lJSON->{'refresh_token'};
	}

	return( $lJSON );
}

sub ssoGetUser {
	my( $aSecret, $aAccessToken ) = @_;

	my $lJSON = callOAuth2API([
		client_id => 'rs_client',
		client_secret => $aSecret,
		grant_type => "urn:pingidentity.com:oauth2:grant_type:validate_bearer",
		token => $aAccessToken
	]);

	if( defined $lJSON->{'access_token'} ) {
		my $lAccess_token = $lJSON->{'access_token'};
		if( defined $lAccess_token->{'username'} ) {
			$username = $lAccess_token->{'username'};
		}
	}

	return( $lJSON );
}

sub ssoRefreshAccessToken {
	my( $aRefreshToken ) = @_;

	my $lJSON = callOAuth2API([
		client_id => 'ro_client',
		grant_type => 'refresh_token',
		refresh_token => $aRefreshToken
	]);

	if( defined $lJSON->{'access_token'} ) {
		$access_token = $lJSON->{'access_token'};
	}

	return( $lJSON );
}


my $res = ssoAuthDirect( $username, $password );
print Dumper( $res );
printf( "Data for getUser: %s, %s\n", $client_secret, $access_token );
$res = ssoGetUser( $client_secret, $access_token );
print Dumper( $res );
printf( "Data for refresh access token: %s\n", $refresh_token );
$res = ssoRefreshAccessToken( $refresh_token );
print Dumper( $res );
