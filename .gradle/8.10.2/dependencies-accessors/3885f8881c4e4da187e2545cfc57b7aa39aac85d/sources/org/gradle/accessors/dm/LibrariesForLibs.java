package org.gradle.accessors.dm;

import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.MinimalExternalModuleDependency;
import org.gradle.plugin.use.PluginDependency;
import org.gradle.api.artifacts.ExternalModuleDependencyBundle;
import org.gradle.api.artifacts.MutableVersionConstraint;
import org.gradle.api.provider.Provider;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.internal.catalog.AbstractExternalDependencyFactory;
import org.gradle.api.internal.catalog.DefaultVersionCatalog;
import java.util.Map;
import org.gradle.api.internal.attributes.ImmutableAttributesFactory;
import org.gradle.api.internal.artifacts.dsl.CapabilityNotationParser;
import javax.inject.Inject;

/**
 * A catalog of dependencies accessible via the {@code libs} extension.
 */
@NonNullApi
public class LibrariesForLibs extends AbstractExternalDependencyFactory {

    private final AbstractExternalDependencyFactory owner = this;
    private final AdcashLibraryAccessors laccForAdcashLibraryAccessors = new AdcashLibraryAccessors(owner);
    private final AndroidxLibraryAccessors laccForAndroidxLibraryAccessors = new AndroidxLibraryAccessors(owner);
    private final FirebaseLibraryAccessors laccForFirebaseLibraryAccessors = new FirebaseLibraryAccessors(owner);
    private final GoogleLibraryAccessors laccForGoogleLibraryAccessors = new GoogleLibraryAccessors(owner);
    private final KotlinLibraryAccessors laccForKotlinLibraryAccessors = new KotlinLibraryAccessors(owner);
    private final KotlinxLibraryAccessors laccForKotlinxLibraryAccessors = new KotlinxLibraryAccessors(owner);
    private final NexusLibraryAccessors laccForNexusLibraryAccessors = new NexusLibraryAccessors(owner);
    private final PlayLibraryAccessors laccForPlayLibraryAccessors = new PlayLibraryAccessors(owner);
    private final VersionAccessors vaccForVersionAccessors = new VersionAccessors(providers, config);
    private final BundleAccessors baccForBundleAccessors = new BundleAccessors(objects, providers, config, attributesFactory, capabilityNotationParser);
    private final PluginAccessors paccForPluginAccessors = new PluginAccessors(providers, config);

    @Inject
    public LibrariesForLibs(DefaultVersionCatalog config, ProviderFactory providers, ObjectFactory objects, ImmutableAttributesFactory attributesFactory, CapabilityNotationParser capabilityNotationParser) {
        super(config, providers, objects, attributesFactory, capabilityNotationParser);
    }

    /**
     * Dependency provider for <b>colorpicker</b> with <b>com.github.QuadFlask:colorpicker</b> coordinates and
     * with version reference <b>colorpicker</b>
     * <p>
     * This dependency was declared in catalog libs.versions.toml
     */
    public Provider<MinimalExternalModuleDependency> getColorpicker() {
        return create("colorpicker");
    }

    /**
     * Dependency provider for <b>flexbox</b> with <b>com.google.android.flexbox:flexbox</b> coordinates and
     * with version reference <b>flexbox</b>
     * <p>
     * This dependency was declared in catalog libs.versions.toml
     */
    public Provider<MinimalExternalModuleDependency> getFlexbox() {
        return create("flexbox");
    }

    /**
     * Dependency provider for <b>glide</b> with <b>com.github.bumptech.glide:glide</b> coordinates and
     * with version reference <b>glide</b>
     * <p>
     * This dependency was declared in catalog libs.versions.toml
     */
    public Provider<MinimalExternalModuleDependency> getGlide() {
        return create("glide");
    }

    /**
     * Dependency provider for <b>gson</b> with <b>com.google.code.gson:gson</b> coordinates and
     * with version reference <b>gson</b>
     * <p>
     * This dependency was declared in catalog libs.versions.toml
     */
    public Provider<MinimalExternalModuleDependency> getGson() {
        return create("gson");
    }

    /**
     * Dependency provider for <b>material</b> with <b>com.google.android.material:material</b> coordinates and
     * with version reference <b>material</b>
     * <p>
     * This dependency was declared in catalog libs.versions.toml
     */
    public Provider<MinimalExternalModuleDependency> getMaterial() {
        return create("material");
    }

    /**
     * Dependency provider for <b>photoView</b> with <b>com.github.chrisbanes:PhotoView</b> coordinates and
     * with version reference <b>photoView</b>
     * <p>
     * This dependency was declared in catalog libs.versions.toml
     */
    public Provider<MinimalExternalModuleDependency> getPhotoView() {
        return create("photoView");
    }

    /**
     * Dependency provider for <b>threeTen</b> with <b>com.jakewharton.threetenabp:threetenabp</b> coordinates and
     * with version reference <b>threeTen</b>
     * <p>
     * This dependency was declared in catalog libs.versions.toml
     */
    public Provider<MinimalExternalModuleDependency> getThreeTen() {
        return create("threeTen");
    }

    /**
     * Group of libraries at <b>adcash</b>
     */
    public AdcashLibraryAccessors getAdcash() {
        return laccForAdcashLibraryAccessors;
    }

    /**
     * Group of libraries at <b>androidx</b>
     */
    public AndroidxLibraryAccessors getAndroidx() {
        return laccForAndroidxLibraryAccessors;
    }

    /**
     * Group of libraries at <b>firebase</b>
     */
    public FirebaseLibraryAccessors getFirebase() {
        return laccForFirebaseLibraryAccessors;
    }

    /**
     * Group of libraries at <b>google</b>
     */
    public GoogleLibraryAccessors getGoogle() {
        return laccForGoogleLibraryAccessors;
    }

    /**
     * Group of libraries at <b>kotlin</b>
     */
    public KotlinLibraryAccessors getKotlin() {
        return laccForKotlinLibraryAccessors;
    }

    /**
     * Group of libraries at <b>kotlinx</b>
     */
    public KotlinxLibraryAccessors getKotlinx() {
        return laccForKotlinxLibraryAccessors;
    }

    /**
     * Group of libraries at <b>nexus</b>
     */
    public NexusLibraryAccessors getNexus() {
        return laccForNexusLibraryAccessors;
    }

    /**
     * Group of libraries at <b>play</b>
     */
    public PlayLibraryAccessors getPlay() {
        return laccForPlayLibraryAccessors;
    }

    /**
     * Group of versions at <b>versions</b>
     */
    public VersionAccessors getVersions() {
        return vaccForVersionAccessors;
    }

    /**
     * Group of bundles at <b>bundles</b>
     */
    public BundleAccessors getBundles() {
        return baccForBundleAccessors;
    }

    /**
     * Group of plugins at <b>plugins</b>
     */
    public PluginAccessors getPlugins() {
        return paccForPluginAccessors;
    }

    public static class AdcashLibraryAccessors extends SubDependencyFactory {
        private final AdcashFacebookLibraryAccessors laccForAdcashFacebookLibraryAccessors = new AdcashFacebookLibraryAccessors(owner);

        public AdcashLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>adfit</b> with <b>com.avatye.adcash:archive-adfit</b> coordinates and
         * with version reference <b>adcash</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getAdfit() {
            return create("adcash.adfit");
        }

        /**
         * Dependency provider for <b>admob</b> with <b>com.avatye.adcash:archive-admob</b> coordinates and
         * with version reference <b>adcash</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getAdmob() {
            return create("adcash.admob");
        }

        /**
         * Dependency provider for <b>applovin</b> with <b>com.avatye.adcash:archive-applovin</b> coordinates and
         * with version reference <b>adcash</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getApplovin() {
            return create("adcash.applovin");
        }

        /**
         * Dependency provider for <b>bom</b> with <b>com.avatye.adcash:product-adcash</b> coordinates and
         * with version reference <b>adcash</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getBom() {
            return create("adcash.bom");
        }

        /**
         * Dependency provider for <b>cauly</b> with <b>com.avatye.adcash:archive-cauly</b> coordinates and
         * with version reference <b>adcash</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getCauly() {
            return create("adcash.cauly");
        }

        /**
         * Dependency provider for <b>mobwith</b> with <b>com.avatye.adcash:archive-mobwith</b> coordinates and
         * with version reference <b>adcash</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getMobwith() {
            return create("adcash.mobwith");
        }

        /**
         * Dependency provider for <b>nam</b> with <b>com.avatye.adcash:archive-nam</b> coordinates and
         * with version reference <b>adcash</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getNam() {
            return create("adcash.nam");
        }

        /**
         * Dependency provider for <b>pangle</b> with <b>com.avatye.adcash:archive-pangle</b> coordinates and
         * with version reference <b>adcash</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getPangle() {
            return create("adcash.pangle");
        }

        /**
         * Dependency provider for <b>unity</b> with <b>com.avatye.adcash:archive-unity</b> coordinates and
         * with version reference <b>adcash</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getUnity() {
            return create("adcash.unity");
        }

        /**
         * Dependency provider for <b>vungle</b> with <b>com.avatye.adcash:archive-vungle</b> coordinates and
         * with version reference <b>adcash</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getVungle() {
            return create("adcash.vungle");
        }

        /**
         * Group of libraries at <b>adcash.facebook</b>
         */
        public AdcashFacebookLibraryAccessors getFacebook() {
            return laccForAdcashFacebookLibraryAccessors;
        }

    }

    public static class AdcashFacebookLibraryAccessors extends SubDependencyFactory {

        public AdcashFacebookLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>audience</b> with <b>com.avatye.adcash:archive-facebook-audience</b> coordinates and
         * with version reference <b>adcash</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getAudience() {
            return create("adcash.facebook.audience");
        }

    }

    public static class AndroidxLibraryAccessors extends SubDependencyFactory {
        private final AndroidxRoomLibraryAccessors laccForAndroidxRoomLibraryAccessors = new AndroidxRoomLibraryAccessors(owner);

        public AndroidxLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>activity</b> with <b>androidx.activity:activity</b> coordinates and
         * with version reference <b>activity</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getActivity() {
            return create("androidx.activity");
        }

        /**
         * Dependency provider for <b>appcompat</b> with <b>androidx.appcompat:appcompat</b> coordinates and
         * with version reference <b>appcompat</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getAppcompat() {
            return create("androidx.appcompat");
        }

        /**
         * Dependency provider for <b>biometric</b> with <b>androidx.biometric:biometric</b> coordinates and
         * with version reference <b>biometric</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getBiometric() {
            return create("androidx.biometric");
        }

        /**
         * Dependency provider for <b>constraintlayout</b> with <b>androidx.constraintlayout:constraintlayout</b> coordinates and
         * with version reference <b>constraintlayout</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getConstraintlayout() {
            return create("androidx.constraintlayout");
        }

        /**
         * Dependency provider for <b>recyclerview</b> with <b>androidx.recyclerview:recyclerview</b> coordinates and
         * with version reference <b>recyclerview</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getRecyclerview() {
            return create("androidx.recyclerview");
        }

        /**
         * Dependency provider for <b>window</b> with <b>androidx.window:window</b> coordinates and
         * with version reference <b>window</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getWindow() {
            return create("androidx.window");
        }

        /**
         * Group of libraries at <b>androidx.room</b>
         */
        public AndroidxRoomLibraryAccessors getRoom() {
            return laccForAndroidxRoomLibraryAccessors;
        }

    }

    public static class AndroidxRoomLibraryAccessors extends SubDependencyFactory {

        public AndroidxRoomLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>compiler</b> with <b>androidx.room:room-compiler</b> coordinates and
         * with version reference <b>roomVersion</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getCompiler() {
            return create("androidx.room.compiler");
        }

        /**
         * Dependency provider for <b>ktx</b> with <b>androidx.room:room-ktx</b> coordinates and
         * with version reference <b>roomVersion</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getKtx() {
            return create("androidx.room.ktx");
        }

        /**
         * Dependency provider for <b>runtime</b> with <b>androidx.room:room-runtime</b> coordinates and
         * with version reference <b>roomVersion</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getRuntime() {
            return create("androidx.room.runtime");
        }

    }

    public static class FirebaseLibraryAccessors extends SubDependencyFactory {
        private final FirebaseConfigLibraryAccessors laccForFirebaseConfigLibraryAccessors = new FirebaseConfigLibraryAccessors(owner);
        private final FirebaseCrashlyticsLibraryAccessors laccForFirebaseCrashlyticsLibraryAccessors = new FirebaseCrashlyticsLibraryAccessors(owner);

        public FirebaseLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>ai</b> with <b>com.google.firebase:firebase-ai</b> coordinates and
         * with version reference <b>firebaseAi</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getAi() {
            return create("firebase.ai");
        }

        /**
         * Dependency provider for <b>analytics</b> with <b>com.google.firebase:firebase-analytics</b> coordinates and
         * with <b>no version specified</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getAnalytics() {
            return create("firebase.analytics");
        }

        /**
         * Dependency provider for <b>bom</b> with <b>com.google.firebase:firebase-bom</b> coordinates and
         * with version reference <b>firebaseBom</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getBom() {
            return create("firebase.bom");
        }

        /**
         * Group of libraries at <b>firebase.config</b>
         */
        public FirebaseConfigLibraryAccessors getConfig() {
            return laccForFirebaseConfigLibraryAccessors;
        }

        /**
         * Group of libraries at <b>firebase.crashlytics</b>
         */
        public FirebaseCrashlyticsLibraryAccessors getCrashlytics() {
            return laccForFirebaseCrashlyticsLibraryAccessors;
        }

    }

    public static class FirebaseConfigLibraryAccessors extends SubDependencyFactory {

        public FirebaseConfigLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>ktx</b> with <b>com.google.firebase:firebase-config-ktx</b> coordinates and
         * with version reference <b>firebaseConfigKtx</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getKtx() {
            return create("firebase.config.ktx");
        }

    }

    public static class FirebaseCrashlyticsLibraryAccessors extends SubDependencyFactory {

        public FirebaseCrashlyticsLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>ndk</b> with <b>com.google.firebase:firebase-crashlytics-ndk</b> coordinates and
         * with version reference <b>firebaseCrashlyticsNdk</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getNdk() {
            return create("firebase.crashlytics.ndk");
        }

    }

    public static class GoogleLibraryAccessors extends SubDependencyFactory {
        private final GoogleApiLibraryAccessors laccForGoogleApiLibraryAccessors = new GoogleApiLibraryAccessors(owner);
        private final GoogleHttpLibraryAccessors laccForGoogleHttpLibraryAccessors = new GoogleHttpLibraryAccessors(owner);
        private final GoogleOauthLibraryAccessors laccForGoogleOauthLibraryAccessors = new GoogleOauthLibraryAccessors(owner);

        public GoogleLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>google.api</b>
         */
        public GoogleApiLibraryAccessors getApi() {
            return laccForGoogleApiLibraryAccessors;
        }

        /**
         * Group of libraries at <b>google.http</b>
         */
        public GoogleHttpLibraryAccessors getHttp() {
            return laccForGoogleHttpLibraryAccessors;
        }

        /**
         * Group of libraries at <b>google.oauth</b>
         */
        public GoogleOauthLibraryAccessors getOauth() {
            return laccForGoogleOauthLibraryAccessors;
        }

    }

    public static class GoogleApiLibraryAccessors extends SubDependencyFactory {
        private final GoogleApiClientLibraryAccessors laccForGoogleApiClientLibraryAccessors = new GoogleApiClientLibraryAccessors(owner);
        private final GoogleApiServicesLibraryAccessors laccForGoogleApiServicesLibraryAccessors = new GoogleApiServicesLibraryAccessors(owner);

        public GoogleApiLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>google.api.client</b>
         */
        public GoogleApiClientLibraryAccessors getClient() {
            return laccForGoogleApiClientLibraryAccessors;
        }

        /**
         * Group of libraries at <b>google.api.services</b>
         */
        public GoogleApiServicesLibraryAccessors getServices() {
            return laccForGoogleApiServicesLibraryAccessors;
        }

    }

    public static class GoogleApiClientLibraryAccessors extends SubDependencyFactory {

        public GoogleApiClientLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>android</b> with <b>com.google.api-client:google-api-client-android</b> coordinates and
         * with version reference <b>googleApiClientAndroid</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getAndroid() {
            return create("google.api.client.android");
        }

        /**
         * Dependency provider for <b>gson</b> with <b>com.google.api-client:google-api-client-gson</b> coordinates and
         * with version reference <b>googleApiClientAndroid</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getGson() {
            return create("google.api.client.gson");
        }

    }

    public static class GoogleApiServicesLibraryAccessors extends SubDependencyFactory {

        public GoogleApiServicesLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>drive</b> with <b>com.google.apis:google-api-services-drive</b> coordinates and
         * with version reference <b>googleApiServicesDrive</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getDrive() {
            return create("google.api.services.drive");
        }

    }

    public static class GoogleHttpLibraryAccessors extends SubDependencyFactory {
        private final GoogleHttpClientLibraryAccessors laccForGoogleHttpClientLibraryAccessors = new GoogleHttpClientLibraryAccessors(owner);

        public GoogleHttpLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>google.http.client</b>
         */
        public GoogleHttpClientLibraryAccessors getClient() {
            return laccForGoogleHttpClientLibraryAccessors;
        }

    }

    public static class GoogleHttpClientLibraryAccessors extends SubDependencyFactory {

        public GoogleHttpClientLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>android</b> with <b>com.google.http-client:google-http-client-android</b> coordinates and
         * with version reference <b>googleHttpClientAndroid</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getAndroid() {
            return create("google.http.client.android");
        }

    }

    public static class GoogleOauthLibraryAccessors extends SubDependencyFactory {
        private final GoogleOauthClientLibraryAccessors laccForGoogleOauthClientLibraryAccessors = new GoogleOauthClientLibraryAccessors(owner);

        public GoogleOauthLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>google.oauth.client</b>
         */
        public GoogleOauthClientLibraryAccessors getClient() {
            return laccForGoogleOauthClientLibraryAccessors;
        }

    }

    public static class GoogleOauthClientLibraryAccessors extends SubDependencyFactory {

        public GoogleOauthClientLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>jetty</b> with <b>com.google.oauth-client:google-oauth-client-jetty</b> coordinates and
         * with version reference <b>googleOauthClientJetty</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getJetty() {
            return create("google.oauth.client.jetty");
        }

    }

    public static class KotlinLibraryAccessors extends SubDependencyFactory {

        public KotlinLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>test</b> with <b>org.jetbrains.kotlin:kotlin-test</b> coordinates and
         * with version reference <b>kotlin</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getTest() {
            return create("kotlin.test");
        }

    }

    public static class KotlinxLibraryAccessors extends SubDependencyFactory {
        private final KotlinxSerializationLibraryAccessors laccForKotlinxSerializationLibraryAccessors = new KotlinxSerializationLibraryAccessors(owner);

        public KotlinxLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>kotlinx.serialization</b>
         */
        public KotlinxSerializationLibraryAccessors getSerialization() {
            return laccForKotlinxSerializationLibraryAccessors;
        }

    }

    public static class KotlinxSerializationLibraryAccessors extends SubDependencyFactory {

        public KotlinxSerializationLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>json</b> with <b>org.jetbrains.kotlinx:kotlinx-serialization-json</b> coordinates and
         * with version reference <b>kotlinxSerializationJson</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getJson() {
            return create("kotlinx.serialization.json");
        }

    }

    public static class NexusLibraryAccessors extends SubDependencyFactory {
        private final NexusKitLibraryAccessors laccForNexusKitLibraryAccessors = new NexusKitLibraryAccessors(owner);

        public NexusLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>nexus.kit</b>
         */
        public NexusKitLibraryAccessors getKit() {
            return laccForNexusKitLibraryAccessors;
        }

    }

    public static class NexusKitLibraryAccessors extends SubDependencyFactory implements DependencyNotationSupplier {

        public NexusKitLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>kit</b> with <b>com.caffeine.common:nexus-kit</b> coordinates and
         * with version reference <b>nexusKit</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> asProvider() {
            return create("nexus.kit");
        }

        /**
         * Dependency provider for <b>v0010</b> with <b>com.caffeine.common:nexus-kit</b> coordinates and
         * with version reference <b>nexusKitVersion</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getV0010() {
            return create("nexus.kit.v0010");
        }

    }

    public static class PlayLibraryAccessors extends SubDependencyFactory {
        private final PlayServicesLibraryAccessors laccForPlayServicesLibraryAccessors = new PlayServicesLibraryAccessors(owner);

        public PlayLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>play.services</b>
         */
        public PlayServicesLibraryAccessors getServices() {
            return laccForPlayServicesLibraryAccessors;
        }

    }

    public static class PlayServicesLibraryAccessors extends SubDependencyFactory {

        public PlayServicesLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>auth</b> with <b>com.google.android.gms:play-services-auth</b> coordinates and
         * with version reference <b>playServicesAuth</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getAuth() {
            return create("play.services.auth");
        }

    }

    public static class VersionAccessors extends VersionFactory  {

        public VersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Version alias <b>activity</b> with value <b>1.10.1</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getActivity() { return getVersion("activity"); }

        /**
         * Version alias <b>adcash</b> with value <b>3.1.3.21_SNAPSHOT</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getAdcash() { return getVersion("adcash"); }

        /**
         * Version alias <b>agp</b> with value <b>8.8.0</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getAgp() { return getVersion("agp"); }

        /**
         * Version alias <b>appcompat</b> with value <b>1.7.0</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getAppcompat() { return getVersion("appcompat"); }

        /**
         * Version alias <b>biometric</b> with value <b>1.1.0</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getBiometric() { return getVersion("biometric"); }

        /**
         * Version alias <b>colorpicker</b> with value <b>0.0.15</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getColorpicker() { return getVersion("colorpicker"); }

        /**
         * Version alias <b>constraintlayout</b> with value <b>2.2.1</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getConstraintlayout() { return getVersion("constraintlayout"); }

        /**
         * Version alias <b>crashLytics</b> with value <b>2.9.9</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getCrashLytics() { return getVersion("crashLytics"); }

        /**
         * Version alias <b>firebaseAi</b> with value <b>17.1.0</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getFirebaseAi() { return getVersion("firebaseAi"); }

        /**
         * Version alias <b>firebaseBom</b> with value <b>32.7.2</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getFirebaseBom() { return getVersion("firebaseBom"); }

        /**
         * Version alias <b>firebaseConfigKtx</b> with value <b>22.1.2</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getFirebaseConfigKtx() { return getVersion("firebaseConfigKtx"); }

        /**
         * Version alias <b>firebaseCrashlyticsNdk</b> with value <b>19.3.0</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getFirebaseCrashlyticsNdk() { return getVersion("firebaseCrashlyticsNdk"); }

        /**
         * Version alias <b>flexbox</b> with value <b>3.0.0</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getFlexbox() { return getVersion("flexbox"); }

        /**
         * Version alias <b>glide</b> with value <b>4.16.0</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getGlide() { return getVersion("glide"); }

        /**
         * Version alias <b>googleApiClientAndroid</b> with value <b>1.35.0</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getGoogleApiClientAndroid() { return getVersion("googleApiClientAndroid"); }

        /**
         * Version alias <b>googleApiServicesDrive</b> with value <b>v3-rev20230815-2.0.0</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getGoogleApiServicesDrive() { return getVersion("googleApiServicesDrive"); }

        /**
         * Version alias <b>googleHttpClientAndroid</b> with value <b>1.35.0</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getGoogleHttpClientAndroid() { return getVersion("googleHttpClientAndroid"); }

        /**
         * Version alias <b>googleOauthClientJetty</b> with value <b>1.34.1</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getGoogleOauthClientJetty() { return getVersion("googleOauthClientJetty"); }

        /**
         * Version alias <b>googleServices</b> with value <b>4.4.3</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getGoogleServices() { return getVersion("googleServices"); }

        /**
         * Version alias <b>gson</b> with value <b>2.10.1</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getGson() { return getVersion("gson"); }

        /**
         * Version alias <b>kotlin</b> with value <b>2.0.21</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getKotlin() { return getVersion("kotlin"); }

        /**
         * Version alias <b>kotlinxSerializationJson</b> with value <b>1.5.1</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getKotlinxSerializationJson() { return getVersion("kotlinxSerializationJson"); }

        /**
         * Version alias <b>ksp</b> with value <b>2.0.21-1.0.27</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getKsp() { return getVersion("ksp"); }

        /**
         * Version alias <b>material</b> with value <b>1.12.0</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getMaterial() { return getVersion("material"); }

        /**
         * Version alias <b>nexusKit</b> with value <b>0.0.1</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getNexusKit() { return getVersion("nexusKit"); }

        /**
         * Version alias <b>nexusKitVersion</b> with value <b>0.0.10</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getNexusKitVersion() { return getVersion("nexusKitVersion"); }

        /**
         * Version alias <b>photoView</b> with value <b>2.3.0</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getPhotoView() { return getVersion("photoView"); }

        /**
         * Version alias <b>playServicesAuth</b> with value <b>21.3.0</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getPlayServicesAuth() { return getVersion("playServicesAuth"); }

        /**
         * Version alias <b>recyclerview</b> with value <b>1.4.0</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getRecyclerview() { return getVersion("recyclerview"); }

        /**
         * Version alias <b>roomVersion</b> with value <b>2.7.2</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getRoomVersion() { return getVersion("roomVersion"); }

        /**
         * Version alias <b>threeTen</b> with value <b>1.4.9</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getThreeTen() { return getVersion("threeTen"); }

        /**
         * Version alias <b>window</b> with value <b>1.4.0</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getWindow() { return getVersion("window"); }

    }

    public static class BundleAccessors extends BundleFactory {

        public BundleAccessors(ObjectFactory objects, ProviderFactory providers, DefaultVersionCatalog config, ImmutableAttributesFactory attributesFactory, CapabilityNotationParser capabilityNotationParser) { super(objects, providers, config, attributesFactory, capabilityNotationParser); }

    }

    public static class PluginAccessors extends PluginFactory {
        private final ComposePluginAccessors paccForComposePluginAccessors = new ComposePluginAccessors(providers, config);

        public PluginAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Plugin provider for <b>androidApplication</b> with plugin id <b>com.android.application</b> and
         * with version reference <b>agp</b>
         * <p>
         * This plugin was declared in catalog libs.versions.toml
         */
        public Provider<PluginDependency> getAndroidApplication() { return createPlugin("androidApplication"); }

        /**
         * Plugin provider for <b>androidLibrary</b> with plugin id <b>com.android.library</b> and
         * with version reference <b>agp</b>
         * <p>
         * This plugin was declared in catalog libs.versions.toml
         */
        public Provider<PluginDependency> getAndroidLibrary() { return createPlugin("androidLibrary"); }

        /**
         * Plugin provider for <b>crashLytics</b> with plugin id <b>com.google.firebase.crashlytics</b> and
         * with version reference <b>crashLytics</b>
         * <p>
         * This plugin was declared in catalog libs.versions.toml
         */
        public Provider<PluginDependency> getCrashLytics() { return createPlugin("crashLytics"); }

        /**
         * Plugin provider for <b>googleServices</b> with plugin id <b>com.google.gms.google-services</b> and
         * with version reference <b>googleServices</b>
         * <p>
         * This plugin was declared in catalog libs.versions.toml
         */
        public Provider<PluginDependency> getGoogleServices() { return createPlugin("googleServices"); }

        /**
         * Plugin provider for <b>kotlinAndroid</b> with plugin id <b>org.jetbrains.kotlin.android</b> and
         * with version reference <b>kotlin</b>
         * <p>
         * This plugin was declared in catalog libs.versions.toml
         */
        public Provider<PluginDependency> getKotlinAndroid() { return createPlugin("kotlinAndroid"); }

        /**
         * Plugin provider for <b>kotlinCocoapods</b> with plugin id <b>org.jetbrains.kotlin.native.cocoapods</b> and
         * with version reference <b>kotlin</b>
         * <p>
         * This plugin was declared in catalog libs.versions.toml
         */
        public Provider<PluginDependency> getKotlinCocoapods() { return createPlugin("kotlinCocoapods"); }

        /**
         * Plugin provider for <b>kotlinMultiplatform</b> with plugin id <b>org.jetbrains.kotlin.multiplatform</b> and
         * with version reference <b>kotlin</b>
         * <p>
         * This plugin was declared in catalog libs.versions.toml
         */
        public Provider<PluginDependency> getKotlinMultiplatform() { return createPlugin("kotlinMultiplatform"); }

        /**
         * Plugin provider for <b>kotlinParcelize</b> with plugin id <b>kotlin-parcelize</b> and
         * with <b>no version specified</b>
         * <p>
         * This plugin was declared in catalog libs.versions.toml
         */
        public Provider<PluginDependency> getKotlinParcelize() { return createPlugin("kotlinParcelize"); }

        /**
         * Plugin provider for <b>ksp</b> with plugin id <b>com.google.devtools.ksp</b> and
         * with version reference <b>ksp</b>
         * <p>
         * This plugin was declared in catalog libs.versions.toml
         */
        public Provider<PluginDependency> getKsp() { return createPlugin("ksp"); }

        /**
         * Group of plugins at <b>plugins.compose</b>
         */
        public ComposePluginAccessors getCompose() {
            return paccForComposePluginAccessors;
        }

    }

    public static class ComposePluginAccessors extends PluginFactory {

        public ComposePluginAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Plugin provider for <b>compose.compiler</b> with plugin id <b>org.jetbrains.kotlin.plugin.compose</b> and
         * with version reference <b>kotlin</b>
         * <p>
         * This plugin was declared in catalog libs.versions.toml
         */
        public Provider<PluginDependency> getCompiler() { return createPlugin("compose.compiler"); }

    }

}
