package com.example.sso;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class SecurityConfig {
	@Value("${SKIP_SSL_VALIDATION:false}")
	private boolean disableSSLValidation = false;

	@Autowired
	private LoggingAccessDeniedHandler accessDeniedHandler;

	@ConditionalOnCloudPlatform(CloudPlatform.CLOUD_FOUNDRY)
	@Configuration
	@EnableWebSecurity
	@EnableOAuth2Sso
	protected class CloudSecurityConfig extends WebSecurityConfigurerAdapter {

		@Override
		protected void configure(final HttpSecurity http) throws Exception {
			http.authorizeRequests()
					.antMatchers("/", "/admin/**", "/error", "/403", "/js/**", "/css/**", "/img/**",
							"/webjars/**").permitAll()
					.antMatchers("/user/**").hasRole("todo.read")
					.anyRequest().authenticated().and()
					.exceptionHandling().accessDeniedHandler(accessDeniedHandler);
		}

		@Bean
		public StrictHttpFirewall httpFirewall() {
			final StrictHttpFirewall firewall = new StrictHttpFirewall();
			firewall.setAllowSemicolon(true);
			return firewall;
		}

		@Bean
		public SSLContext sslContext() {
			log.info("Disabling SSL Validation : {}", disableSSLValidation);
			if (disableSSLValidation) {
				return SSLValidationDisabler.disableSSLValidation();
			} else {
				return SSLValidationDisabler.getSSLConext();
			}
		}
	}

	@Profile("!cloud")
	@Configuration
	@EnableWebSecurity
	protected class LocalSecurityConfig extends WebSecurityConfigurerAdapter {

		@Override
		protected void configure(final HttpSecurity http) throws Exception {
			http.authorizeRequests()
					.antMatchers("/", "/admin/**", "/error", "/403", "/js/**", "/css/**", "/img/**", "/webjars/**")
					.permitAll()
					.antMatchers("/user/**").hasRole("USER").anyRequest().authenticated().and()
					.formLogin().loginPage("/login").permitAll().and()
					.logout().invalidateHttpSession(true).clearAuthentication(true)
					.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
					.logoutSuccessUrl("/login?logout").permitAll().and()
					.exceptionHandling().accessDeniedHandler(accessDeniedHandler);
		}

		@Override
		protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
			auth.inMemoryAuthentication()
			.withUser("user").password("password").roles("USER").and()
			.withUser("manager").password("password").roles("MANAGER");
		}

	}

	public static class SSLValidationDisabler {
		public static SSLContext getSSLConext() {
			try {
				return SSLContext.getInstance("SSL");
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
		}

		public static SSLContext disableSSLValidation() {
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}

				public void checkClientTrusted(final X509Certificate[] certs, String authType) {
				}

				public void checkServerTrusted(final X509Certificate[] certs, String authType) {
				}
			} };

			// Install the all-trusting trust manager
			try {
				final SSLContext sc = getSSLConext();
				sc.init(null, trustAllCerts, new SecureRandom());
				HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
				HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
					@Override
					public boolean verify(final String hostname, final SSLSession session) {
						return true;
					}
				});
				return sc;
			} catch (GeneralSecurityException e) {
				throw new RuntimeException(e);
			}
		}
	}
}