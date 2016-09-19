require tegra-binaries-${PV}.inc
require tegra-shared-binaries.inc

DEPENDS = " \
	${@bb.utils.contains("DISTRO_FEATURES", "x11", "mesa", "", d)} \
"
inherit update-rc.d systemd

do_configure() {
    tar -C ${B} -x -f ${S}/nv_tegra/nvidia_drivers.tbz2 usr/lib usr/sbin var/nvidia
}

do_compile[noexec] = "1"

DRVROOT = "${B}/usr/lib/aarch64-linux-gnu"

do_install() {
    install -d ${D}${localstatedir}
    cp -R ${B}/var/nvidia ${D}${localstatedir}/
    install -d ${D}${libdir}/libv4l/plugins
    for f in ${DRVROOT}/libv4l/plugins/lib*; do
        install -m 0644 $f ${D}${libdir}/libv4l/plugins/
    done
    install -d ${D}${libdir}
    for f in ${DRVROOT}/tegra/lib*; do    
        install -m 0644 $f ${D}${libdir}
    done
    # FIXME:
    rm -f ${D}${libdir}/libdrm.so*
    # :EMXIF
    for f in ${DRVROOT}/tegra-egl/lib*; do    
        install -m 0644 $f ${D}${libdir}
    done
    ln -sf libcuda.so.1.1 ${D}${libdir}/libcuda.so
    ln -sf libGL.so.1 ${D}${libdir}/libGL.so
    ln -sf libEGL.so.1 ${D}${libdir}/libEGL.so
    ln -sf libGLESv1_CM.so.1 ${D}${libdir}/libGLESv1_CM.so
    ln -sf libGLESv2.so.2 ${D}${libdir}/libGLESv2.so
    # nvcamera_daemon only looks in this special subdir, so symlink it
    install -d ${D}${nonarch_libdir}/aarch64-linux-gnu
    ln -snf ${libdir} ${D}${nonarch_libdir}/aarch64-linux-gnu/tegra-egl
    install -d ${D}${sbindir}
    install -m755 ${B}${sbindir}/nvcamera-daemon ${D}${sbindir}/
    install -d ${D}${sysconfdir}/init.d
    install -m755 ${S}/nvcamera-daemon.init ${D}${sysconfdir}/init.d/nvcamera-daemon
    install -d ${D}${systemd_system_unitdir}
    install -m644 ${S}/nvcamera-daemon.service ${D}${systemd_system_unitdir}
}

PACKAGES = "${PN}-libv4l-plugins ${PN}"
PROVIDES += "virtual/libgl virtual/libgles1 virtual/libgles2 virtual/egl"

FILES_${PN}-libv4l-plugins = "${libdir}/libv4l"
FILES_${PN} = "${libdir} ${sbindir} ${nonarch_libdir} ${localstatedir} ${sysconfdir}"
RDEPENDS_${PN} = "alsa-lib"

INITSCRIPT_PACKAGES = "${PN}"
INITSCRIPT_NAME_${PN} = "nvcamera-daemon"
INITSCRIPT_PARAMS_${PN} = "defaults"
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE_${PN} = "nvcamera-daemon.service"

INSANE_SKIP_${PN}-libv4l-plugins = "dev-so textrel ldflags build-deps"
INSANE_SKIP_${PN} = "dev-so textrel ldflags build-deps"
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_SYSROOT_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
