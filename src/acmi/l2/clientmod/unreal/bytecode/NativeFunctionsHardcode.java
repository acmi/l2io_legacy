/*
 * Copyright (c) 2014 acmi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package acmi.l2.clientmod.unreal.bytecode;

import acmi.l2.clientmod.unreal.UnrealException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NativeFunctionsHardcode implements NativeFunctionsSupplier {
    private static final Map<Integer, NativeFunction> NATIVE_FUNCTIONS = new HashMap<>();

    static {
        NATIVE_FUNCTIONS.put(112, new NativeFunction("$", false, 40, true));
        NATIVE_FUNCTIONS.put(113, new NativeFunction("GotoState", false, 0, false));
        NATIVE_FUNCTIONS.put(114, new NativeFunction("==", false, 24, true));
        NATIVE_FUNCTIONS.put(115, new NativeFunction("<", false, 24, true));
        NATIVE_FUNCTIONS.put(116, new NativeFunction(">", false, 24, true));
        NATIVE_FUNCTIONS.put(117, new NativeFunction("Enable", false, 0, false));
        NATIVE_FUNCTIONS.put(118, new NativeFunction("Disable", false, 0, false));
        NATIVE_FUNCTIONS.put(119, new NativeFunction("!=", false, 26, true));
        NATIVE_FUNCTIONS.put(120, new NativeFunction("<=", false, 24, true));
        NATIVE_FUNCTIONS.put(121, new NativeFunction(">=", false, 24, true));
        NATIVE_FUNCTIONS.put(122, new NativeFunction("==", false, 24, true));
        NATIVE_FUNCTIONS.put(123, new NativeFunction("!=", false, 26, true));
        NATIVE_FUNCTIONS.put(124, new NativeFunction("~=", false, 24, true));
        NATIVE_FUNCTIONS.put(125, new NativeFunction("Len", false, 0, false));
        NATIVE_FUNCTIONS.put(126, new NativeFunction("InStr", false, 0, false));
        NATIVE_FUNCTIONS.put(127, new NativeFunction("Mid", false, 0, false));
        NATIVE_FUNCTIONS.put(128, new NativeFunction("Left", false, 0, false));
        NATIVE_FUNCTIONS.put(129, new NativeFunction("!", true, 0, true));
        NATIVE_FUNCTIONS.put(130, new NativeFunction("&&", false, 30, true));
        NATIVE_FUNCTIONS.put(131, new NativeFunction("^^", false, 30, true));
        NATIVE_FUNCTIONS.put(132, new NativeFunction("||", false, 32, true));
        NATIVE_FUNCTIONS.put(133, new NativeFunction("*=", false, 34, true));
        NATIVE_FUNCTIONS.put(134, new NativeFunction("/=", false, 34, true));
        NATIVE_FUNCTIONS.put(135, new NativeFunction("+=", false, 34, true));
        NATIVE_FUNCTIONS.put(136, new NativeFunction("-=", false, 34, true));
        NATIVE_FUNCTIONS.put(137, new NativeFunction("++", true, 0, true));
        NATIVE_FUNCTIONS.put(138, new NativeFunction("--", true, 0, true));
        NATIVE_FUNCTIONS.put(139, new NativeFunction("++", false, 0, true));
        NATIVE_FUNCTIONS.put(140, new NativeFunction("--", false, 0, true));
        NATIVE_FUNCTIONS.put(141, new NativeFunction("~", true, 0, true));
        NATIVE_FUNCTIONS.put(142, new NativeFunction("==", false, 24, true));
        NATIVE_FUNCTIONS.put(143, new NativeFunction("-", true, 0, true));
        NATIVE_FUNCTIONS.put(144, new NativeFunction("*", false, 16, true));
        NATIVE_FUNCTIONS.put(145, new NativeFunction("/", false, 16, true));
        NATIVE_FUNCTIONS.put(146, new NativeFunction("+", false, 20, true));
        NATIVE_FUNCTIONS.put(147, new NativeFunction("-", false, 20, true));
        NATIVE_FUNCTIONS.put(148, new NativeFunction("<<", false, 22, true));
        NATIVE_FUNCTIONS.put(149, new NativeFunction(">>", false, 22, true));
        NATIVE_FUNCTIONS.put(150, new NativeFunction("<", false, 24, true));
        NATIVE_FUNCTIONS.put(151, new NativeFunction(">", false, 24, true));
        NATIVE_FUNCTIONS.put(152, new NativeFunction("<=", false, 24, true));
        NATIVE_FUNCTIONS.put(153, new NativeFunction(">=", false, 24, true));
        NATIVE_FUNCTIONS.put(154, new NativeFunction("==", false, 24, true));
        NATIVE_FUNCTIONS.put(155, new NativeFunction("!=", false, 26, true));
        NATIVE_FUNCTIONS.put(156, new NativeFunction("&", false, 28, true));
        NATIVE_FUNCTIONS.put(157, new NativeFunction("^", false, 28, true));
        NATIVE_FUNCTIONS.put(158, new NativeFunction("|", false, 28, true));
        NATIVE_FUNCTIONS.put(159, new NativeFunction("*=", false, 34, true));
        NATIVE_FUNCTIONS.put(160, new NativeFunction("/=", false, 34, true));
        NATIVE_FUNCTIONS.put(161, new NativeFunction("+=", false, 34, true));
        NATIVE_FUNCTIONS.put(162, new NativeFunction("-=", false, 34, true));
        NATIVE_FUNCTIONS.put(163, new NativeFunction("++", true, 0, true));
        NATIVE_FUNCTIONS.put(164, new NativeFunction("--", true, 0, true));
        NATIVE_FUNCTIONS.put(165, new NativeFunction("++", false, 0, true));
        NATIVE_FUNCTIONS.put(166, new NativeFunction("--", false, 0, true));
        NATIVE_FUNCTIONS.put(167, new NativeFunction("Rand", false, 0, false));
        NATIVE_FUNCTIONS.put(168, new NativeFunction("@", false, 40, true));
        NATIVE_FUNCTIONS.put(169, new NativeFunction("-", true, 0, true));
        NATIVE_FUNCTIONS.put(170, new NativeFunction("**", false, 12, true));
        NATIVE_FUNCTIONS.put(171, new NativeFunction("*", false, 16, true));
        NATIVE_FUNCTIONS.put(172, new NativeFunction("/", false, 16, true));
        NATIVE_FUNCTIONS.put(173, new NativeFunction("%", false, 18, true));
        NATIVE_FUNCTIONS.put(174, new NativeFunction("+", false, 20, true));
        NATIVE_FUNCTIONS.put(175, new NativeFunction("-", false, 20, true));
        NATIVE_FUNCTIONS.put(176, new NativeFunction("<", false, 24, true));
        NATIVE_FUNCTIONS.put(177, new NativeFunction(">", false, 24, true));
        NATIVE_FUNCTIONS.put(178, new NativeFunction("<=", false, 24, true));
        NATIVE_FUNCTIONS.put(179, new NativeFunction(">=", false, 24, true));
        NATIVE_FUNCTIONS.put(180, new NativeFunction("==", false, 24, true));
        NATIVE_FUNCTIONS.put(181, new NativeFunction("!=", false, 26, true));
        NATIVE_FUNCTIONS.put(182, new NativeFunction("*=", false, 34, true));
        NATIVE_FUNCTIONS.put(183, new NativeFunction("/=", false, 34, true));
        NATIVE_FUNCTIONS.put(184, new NativeFunction("+=", false, 34, true));
        NATIVE_FUNCTIONS.put(185, new NativeFunction("-=", false, 34, true));
        NATIVE_FUNCTIONS.put(186, new NativeFunction("Abs", false, 0, false));
        NATIVE_FUNCTIONS.put(187, new NativeFunction("Sin", false, 0, false));
        NATIVE_FUNCTIONS.put(188, new NativeFunction("Cos", false, 0, false));
        NATIVE_FUNCTIONS.put(189, new NativeFunction("Tan", false, 0, false));
        NATIVE_FUNCTIONS.put(190, new NativeFunction("Atan", false, 0, false));
        NATIVE_FUNCTIONS.put(191, new NativeFunction("Exp", false, 0, false));
        NATIVE_FUNCTIONS.put(192, new NativeFunction("Loge", false, 0, false));
        NATIVE_FUNCTIONS.put(193, new NativeFunction("Sqrt", false, 0, false));
        NATIVE_FUNCTIONS.put(194, new NativeFunction("Square", false, 0, false));
        NATIVE_FUNCTIONS.put(195, new NativeFunction("FRand", false, 0, false));
        NATIVE_FUNCTIONS.put(196, new NativeFunction(">>>", false, 22, true));
        NATIVE_FUNCTIONS.put(197, new NativeFunction("*", false, 16, true));
        NATIVE_FUNCTIONS.put(203, new NativeFunction("!=", false, 26, true));
        NATIVE_FUNCTIONS.put(210, new NativeFunction("~=", false, 24, true));
        NATIVE_FUNCTIONS.put(211, new NativeFunction("-", true, 0, true));
        NATIVE_FUNCTIONS.put(212, new NativeFunction("*", false, 16, true));
        NATIVE_FUNCTIONS.put(213, new NativeFunction("*", false, 16, true));
        NATIVE_FUNCTIONS.put(214, new NativeFunction("/", false, 16, true));
        NATIVE_FUNCTIONS.put(215, new NativeFunction("+", false, 20, true));
        NATIVE_FUNCTIONS.put(216, new NativeFunction("-", false, 20, true));
        NATIVE_FUNCTIONS.put(217, new NativeFunction("==", false, 24, true));
        NATIVE_FUNCTIONS.put(218, new NativeFunction("!=", false, 26, true));
        NATIVE_FUNCTIONS.put(219, new NativeFunction("Dot", false, 16, true));
        NATIVE_FUNCTIONS.put(220, new NativeFunction("Cross", false, 16, true));
        NATIVE_FUNCTIONS.put(221, new NativeFunction("*=", false, 34, true));
        NATIVE_FUNCTIONS.put(222, new NativeFunction("/=", false, 34, true));
        NATIVE_FUNCTIONS.put(223, new NativeFunction("+=", false, 34, true));
        NATIVE_FUNCTIONS.put(224, new NativeFunction("-=", false, 34, true));
        NATIVE_FUNCTIONS.put(225, new NativeFunction("VSize", false, 0, false));
        NATIVE_FUNCTIONS.put(226, new NativeFunction("Normal", false, 0, false));
        NATIVE_FUNCTIONS.put(227, new NativeFunction("Invert", false, 0, false));
        NATIVE_FUNCTIONS.put(229, new NativeFunction("GetAxes", false, 0, false));
        NATIVE_FUNCTIONS.put(230, new NativeFunction("GetUnAxes", false, 0, false));
        NATIVE_FUNCTIONS.put(231, new NativeFunction("Log", false, 0, false));
        NATIVE_FUNCTIONS.put(232, new NativeFunction("Warn", false, 0, false));
        NATIVE_FUNCTIONS.put(233, new NativeFunction("Error", false, 0, false));
        NATIVE_FUNCTIONS.put(234, new NativeFunction("Right", false, 0, false));
        NATIVE_FUNCTIONS.put(235, new NativeFunction("Caps", false, 0, false));
        NATIVE_FUNCTIONS.put(236, new NativeFunction("Chr", false, 0, false));
        NATIVE_FUNCTIONS.put(237, new NativeFunction("Asc", false, 0, false));
        NATIVE_FUNCTIONS.put(238, new NativeFunction("Substitute", false, 0, false));
        NATIVE_FUNCTIONS.put(242, new NativeFunction("==", false, 24, true));
        NATIVE_FUNCTIONS.put(243, new NativeFunction("!=", false, 26, true));
        NATIVE_FUNCTIONS.put(244, new NativeFunction("FMin", false, 0, false));
        NATIVE_FUNCTIONS.put(245, new NativeFunction("FMax", false, 0, false));
        NATIVE_FUNCTIONS.put(246, new NativeFunction("FClamp", false, 0, false));
        NATIVE_FUNCTIONS.put(247, new NativeFunction("Lerp", false, 0, false));
        NATIVE_FUNCTIONS.put(248, new NativeFunction("Smerp", false, 0, false));
        NATIVE_FUNCTIONS.put(249, new NativeFunction("Min", false, 0, false));
        NATIVE_FUNCTIONS.put(250, new NativeFunction("Max", false, 0, false));
        NATIVE_FUNCTIONS.put(251, new NativeFunction("Clamp", false, 0, false));
        NATIVE_FUNCTIONS.put(252, new NativeFunction("VRand", false, 0, false));
        NATIVE_FUNCTIONS.put(254, new NativeFunction("==", false, 24, true));
        NATIVE_FUNCTIONS.put(255, new NativeFunction("!=", false, 26, true));
        NATIVE_FUNCTIONS.put(256, new NativeFunction("Sleep", false, 0, false));
        NATIVE_FUNCTIONS.put(258, new NativeFunction("ClassIsChildOf", false, 0, false));
        NATIVE_FUNCTIONS.put(259, new NativeFunction("PlayAnim", false, 0, false));
        NATIVE_FUNCTIONS.put(260, new NativeFunction("LoopAnim", false, 0, false));
        NATIVE_FUNCTIONS.put(261, new NativeFunction("FinishAnim", false, 0, false));
        NATIVE_FUNCTIONS.put(262, new NativeFunction("SetCollision", false, 0, false));
        NATIVE_FUNCTIONS.put(263, new NativeFunction("HasAnim", false, 0, false));
        NATIVE_FUNCTIONS.put(264, new NativeFunction("PlaySound", false, 0, false));
        NATIVE_FUNCTIONS.put(266, new NativeFunction("Move", false, 0, false));
        NATIVE_FUNCTIONS.put(267, new NativeFunction("SetLocation", false, 0, false));
        NATIVE_FUNCTIONS.put(272, new NativeFunction("SetOwner", false, 0, false));
        NATIVE_FUNCTIONS.put(275, new NativeFunction("<<", false, 22, true));
        NATIVE_FUNCTIONS.put(276, new NativeFunction(">>", false, 22, true));
        NATIVE_FUNCTIONS.put(277, new NativeFunction("Trace", false, 0, false));
        NATIVE_FUNCTIONS.put(278, new NativeFunction("Spawn", false, 0, false));
        NATIVE_FUNCTIONS.put(279, new NativeFunction("Destroy", false, 0, false));
        NATIVE_FUNCTIONS.put(280, new NativeFunction("SetTimer", false, 0, false));
        NATIVE_FUNCTIONS.put(281, new NativeFunction("IsInState", false, 0, false));
        NATIVE_FUNCTIONS.put(282, new NativeFunction("IsAnimating", false, 0, false));
        NATIVE_FUNCTIONS.put(283, new NativeFunction("SetCollisionSize", false, 0, false));
        NATIVE_FUNCTIONS.put(284, new NativeFunction("GetStateName", false, 0, false));
        NATIVE_FUNCTIONS.put(287, new NativeFunction("*", false, 16, true));
        NATIVE_FUNCTIONS.put(288, new NativeFunction("*", false, 16, true));
        NATIVE_FUNCTIONS.put(289, new NativeFunction("/", false, 16, true));
        NATIVE_FUNCTIONS.put(290, new NativeFunction("*=", false, 34, true));
        NATIVE_FUNCTIONS.put(291, new NativeFunction("/=", false, 34, true));
        NATIVE_FUNCTIONS.put(294, new NativeFunction("TweenAnim", false, 0, false));
        NATIVE_FUNCTIONS.put(296, new NativeFunction("*", false, 16, true));
        NATIVE_FUNCTIONS.put(297, new NativeFunction("*=", false, 34, true));
        NATIVE_FUNCTIONS.put(298, new NativeFunction("SetBase", false, 0, false));
        NATIVE_FUNCTIONS.put(299, new NativeFunction("SetRotation", false, 0, false));
        NATIVE_FUNCTIONS.put(300, new NativeFunction("MirrorVectorByNormal", false, 0, false));
        NATIVE_FUNCTIONS.put(301, new NativeFunction("FinishInterpolation", false, 0, false));
        NATIVE_FUNCTIONS.put(303, new NativeFunction("IsA", false, 0, false));
        NATIVE_FUNCTIONS.put(304, new NativeFunction("AllActors", false, 0, false));
        NATIVE_FUNCTIONS.put(305, new NativeFunction("ChildActors", false, 0, false));
        NATIVE_FUNCTIONS.put(306, new NativeFunction("BasedActors", false, 0, false));
        NATIVE_FUNCTIONS.put(307, new NativeFunction("TouchingActors", false, 0, false));
        NATIVE_FUNCTIONS.put(308, new NativeFunction("ZoneActors", false, 0, false));
        NATIVE_FUNCTIONS.put(309, new NativeFunction("TraceActors", false, 0, false));
        NATIVE_FUNCTIONS.put(310, new NativeFunction("RadiusActors", false, 0, false));
        NATIVE_FUNCTIONS.put(311, new NativeFunction("VisibleActors", false, 0, false));
        NATIVE_FUNCTIONS.put(312, new NativeFunction("VisibleCollidingActors", false, 0, false));
        NATIVE_FUNCTIONS.put(313, new NativeFunction("DynamicActors", false, 0, false));
        NATIVE_FUNCTIONS.put(314, new NativeFunction("Warp", false, 0, false));
        NATIVE_FUNCTIONS.put(315, new NativeFunction("UnWarp", false, 0, false));
        NATIVE_FUNCTIONS.put(316, new NativeFunction("+", false, 20, true));
        NATIVE_FUNCTIONS.put(317, new NativeFunction("-", false, 20, true));
        NATIVE_FUNCTIONS.put(318, new NativeFunction("+=", false, 34, true));
        NATIVE_FUNCTIONS.put(319, new NativeFunction("-=", false, 34, true));
        NATIVE_FUNCTIONS.put(320, new NativeFunction("RotRand", false, 0, false));
        NATIVE_FUNCTIONS.put(321, new NativeFunction("CollidingActors", false, 0, false));
        NATIVE_FUNCTIONS.put(400, new NativeFunction("-", true, 0, true));
        NATIVE_FUNCTIONS.put(401, new NativeFunction("*", false, 16, true));
        NATIVE_FUNCTIONS.put(402, new NativeFunction("*", false, 16, true));
        NATIVE_FUNCTIONS.put(403, new NativeFunction("/", false, 16, true));
        NATIVE_FUNCTIONS.put(404, new NativeFunction("+", false, 20, true));
        NATIVE_FUNCTIONS.put(405, new NativeFunction("-", false, 20, true));
        NATIVE_FUNCTIONS.put(406, new NativeFunction("+=", false, 34, true));
        NATIVE_FUNCTIONS.put(407, new NativeFunction("-=", false, 34, true));
        NATIVE_FUNCTIONS.put(408, new NativeFunction("<", false, 24, true));
        NATIVE_FUNCTIONS.put(409, new NativeFunction(">", false, 24, true));
        NATIVE_FUNCTIONS.put(410, new NativeFunction("<=", false, 24, true));
        NATIVE_FUNCTIONS.put(411, new NativeFunction(">=", false, 24, true));
        NATIVE_FUNCTIONS.put(412, new NativeFunction("==", false, 24, true));
        NATIVE_FUNCTIONS.put(413, new NativeFunction("!=", false, 26, true));
        NATIVE_FUNCTIONS.put(464, new NativeFunction("StrLen", false, 0, false));
        NATIVE_FUNCTIONS.put(465, new NativeFunction("DrawText", false, 0, false));
        NATIVE_FUNCTIONS.put(466, new NativeFunction("DrawTile", false, 0, false));
        NATIVE_FUNCTIONS.put(467, new NativeFunction("DrawActor", false, 0, false));
        NATIVE_FUNCTIONS.put(468, new NativeFunction("DrawTileClipped", false, 0, false));
        NATIVE_FUNCTIONS.put(469, new NativeFunction("DrawTextClipped", false, 0, false));
        NATIVE_FUNCTIONS.put(470, new NativeFunction("TextSize", false, 0, false));
        NATIVE_FUNCTIONS.put(480, new NativeFunction("DrawPortal", false, 0, false));
        NATIVE_FUNCTIONS.put(500, new NativeFunction("MoveTo", false, 0, false));
        NATIVE_FUNCTIONS.put(502, new NativeFunction("MoveToward", false, 0, false));
        NATIVE_FUNCTIONS.put(508, new NativeFunction("FinishRotation", false, 0, false));
        NATIVE_FUNCTIONS.put(510, new NativeFunction("WaitToSeeEnemy", false, 0, false));
        NATIVE_FUNCTIONS.put(512, new NativeFunction("MakeNoise", false, 0, false));
        NATIVE_FUNCTIONS.put(514, new NativeFunction("LineOfSightTo", false, 0, false));
        NATIVE_FUNCTIONS.put(517, new NativeFunction("FindPathToward", false, 0, false));
        NATIVE_FUNCTIONS.put(518, new NativeFunction("FindPathTo", false, 0, false));
        NATIVE_FUNCTIONS.put(520, new NativeFunction("actorReachable", false, 0, false));
        NATIVE_FUNCTIONS.put(521, new NativeFunction("pointReachable", false, 0, false));
        NATIVE_FUNCTIONS.put(523, new NativeFunction("EAdjustJump", false, 0, false));
        NATIVE_FUNCTIONS.put(524, new NativeFunction("FindStairRotation", false, 0, false));
        NATIVE_FUNCTIONS.put(525, new NativeFunction("FindRandomDest", false, 0, false));
        NATIVE_FUNCTIONS.put(526, new NativeFunction("PickWallAdjust", false, 0, false));
        NATIVE_FUNCTIONS.put(527, new NativeFunction("WaitForLanding", false, 0, false));
        NATIVE_FUNCTIONS.put(529, new NativeFunction("AddController", false, 0, false));
        NATIVE_FUNCTIONS.put(530, new NativeFunction("RemoveController", false, 0, false));
        NATIVE_FUNCTIONS.put(531, new NativeFunction("PickTarget", false, 0, false));
        NATIVE_FUNCTIONS.put(532, new NativeFunction("PlayerCanSeeMe", false, 0, false));
        NATIVE_FUNCTIONS.put(533, new NativeFunction("CanSee", false, 0, false));
        NATIVE_FUNCTIONS.put(534, new NativeFunction("PickAnyTarget", false, 0, false));
        NATIVE_FUNCTIONS.put(536, new NativeFunction("SaveConfig", false, 0, false));
        NATIVE_FUNCTIONS.put(539, new NativeFunction("GetMapName", false, 0, false));
        NATIVE_FUNCTIONS.put(540, new NativeFunction("FindBestInventoryPath", false, 0, false));
        NATIVE_FUNCTIONS.put(544, new NativeFunction("ResetKeyboard", false, 0, false));
        NATIVE_FUNCTIONS.put(545, new NativeFunction("GetNextSkin", false, 0, false));
        NATIVE_FUNCTIONS.put(546, new NativeFunction("UpdateURL", false, 0, false));
        NATIVE_FUNCTIONS.put(547, new NativeFunction("GetURLMap", false, 0, false));
        NATIVE_FUNCTIONS.put(548, new NativeFunction("FastTrace", false, 0, false));
        NATIVE_FUNCTIONS.put(549, new NativeFunction("-", false, 20, true));
        NATIVE_FUNCTIONS.put(550, new NativeFunction("*", false, 16, true));
        NATIVE_FUNCTIONS.put(551, new NativeFunction("+", false, 20, true));
        NATIVE_FUNCTIONS.put(552, new NativeFunction("*", false, 16, true));
        NATIVE_FUNCTIONS.put(3969, new NativeFunction("MoveSmooth", false, 0, false));
        NATIVE_FUNCTIONS.put(3970, new NativeFunction("SetPhysics", false, 0, false));
        NATIVE_FUNCTIONS.put(3971, new NativeFunction("AutonomousPhysics", false, 0, false));
    }

    @Override
    public NativeFunction apply(Integer integer) throws UnrealException {
        return Optional.ofNullable(NATIVE_FUNCTIONS.get(integer)).orElseThrow(() -> new UnrealException(String.format("Native function (%d) not found", integer)));
    }
}
